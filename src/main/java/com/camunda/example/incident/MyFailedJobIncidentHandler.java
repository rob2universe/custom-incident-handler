package com.camunda.example.incident;

import com.camunda.example.dto.ErrorForIncident;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.incident.DefaultIncidentHandler;
import org.camunda.bpm.engine.impl.incident.IncidentContext;
import org.camunda.bpm.engine.impl.incident.IncidentHandler;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.model.bpmn.BpmnModelException;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class MyFailedJobIncidentHandler extends DefaultIncidentHandler implements IncidentHandler {

  public MyFailedJobIncidentHandler(String type) {
    super(type);
  }

  @Getter
  private final List<String> activityIds = new ArrayList<>();

  @Override
  public Incident handleIncident(IncidentContext context, String message) {
    RuntimeService runtimeService = Context.getProcessEngineConfiguration().getRuntimeService();
    ManagementService managementService = Context.getProcessEngineConfiguration().getManagementService();
    RepositoryService repositoryService = Context.getProcessEngineConfiguration().getRepositoryService();

    activityIds.add(context.getActivityId());
    log.info("MY_FAILED_JOB_INCIDENT: " + message);

    var modelinstance = repositoryService.getBpmnModelInstance(context.getProcessDefinitionId());
    if(context.getFailedActivityId() !=null) {

      CamundaProperties camProps;
      boolean signalIncident = false;
      String signalName = null;
      Activity activity = modelinstance.getModelElementById(context.getFailedActivityId());
      // if extension properties exist use them
      try {
        camProps = activity.getExtensionElements().getElementsQuery()
          .filterByType(CamundaProperties.class).singleResult();
        // read extension properties
        if (camProps != null) {
            for (CamundaProperty prop : camProps.getCamundaProperties()) {
              log.debug("Camunda property {} with value {}", prop.getCamundaName(), prop.getCamundaValue());
              if (prop.getCamundaName().equals("signalIncident") && Boolean.parseBoolean(prop.getCamundaValue()))
                  signalIncident = true;
              if (prop.getCamundaName().equals("signalName"))
                      signalName = prop.getCamundaValue();
              }
          }
      }
      catch (BpmnModelException e)
      {
          log.debug("No extension elements found on failed activity with id: {}", context.getFailedActivityId());
      }

      // if signalIncident extension property is set to true and signal name is set, send signal
      if (signalIncident && null != signalName) {
        log.info("Sending signal {} ...", signalName);
        var executionId = context.getExecutionId();

        ProcessInstance pi = (ProcessInstance) runtimeService.createExecutionQuery().executionId(executionId).singleResult();

        Job job = managementService.createJobQuery()
            .jobDefinitionId(context.getJobDefinitionId())
            .executionId(executionId)
            .singleResult();

        //create DTO
        ErrorForIncident errorForIncident = new ErrorForIncident();
        errorForIncident.setActivityId(context.getFailedActivityId());
        errorForIncident.setProcessInstance(pi.getId());
        errorForIncident.setErrorMessage(message);
        errorForIncident.setJobId(job.getId());
        errorForIncident.setBusinessKey(pi.getBusinessKey());

        //serialize to JSON process data
        ObjectValue errorObj = Variables.objectValue(errorForIncident)
            .serializationDataFormat(Variables.SerializationDataFormats.JSON)
            .create();
        log.info("errorForIncident object created: {}", errorObj);

        //send Signal with error info as payload
        runtimeService.createSignalEvent(signalName)
            .setVariables(Map.of("errorForIncident", errorObj))
            .send();
      }
    }

    return super.handleIncident(context, message);
  }
}
