package com.camunda.example;

import com.camunda.example.incident.MyFailedJobIncidentHandler;
import com.camunda.example.incident.MyIncidentHandler;
import com.camunda.example.service.MyDelegate;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.spring.boot.starter.test.helper.AbstractProcessEngineRuleTest;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.camunda.example.incident.MyIncidentHandler.INCIDENT_TYPE;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@Deployment(resources = "process.bpmn")
public class CustomIncidentHandlerTest extends AbstractProcessEngineRuleTest {

  @Before
  public void setUp() {

/*
    processEngine.getProcessEngineConfiguration().setProcessEnginePlugins(
        List.of(new IncidentHandlerProcessEnginePlugin())
    );
*/
// TODO figure out plugin registration for unit test
    processEngine.getProcessEngineConfiguration().setCustomIncidentHandlers(List.of(
        // existing standard incident types
        new MyFailedJobIncidentHandler(Incident.FAILED_JOB_HANDLER_TYPE),
        new MyFailedJobIncidentHandler(Incident.EXTERNAL_TASK_HANDLER_TYPE),
        // custom incident type
        new MyIncidentHandler(INCIDENT_TYPE))
    );

    Mocks.register("myDelegate", new MyDelegate());
  }

  @Test
  public void testMyFailedJobHandler() {
    var pi = runtimeService().startProcessInstanceByKey("error-process", withVariables(MyDelegate.ERROR_DATA, MyDelegate.FAILED_JOB));
    assertThat(pi).isWaitingAt("DoSomethingTask");
    try {
      execute(job());
    } catch (RuntimeException ex) {
      log.info("Exception with Message: {}", ex.getMessage());
      assertEquals(ex.getMessage(), MyDelegate.JOB_FAILED_MESSAGE);
    }
    try {
      execute(job());
    } catch (RuntimeException ex) {
    }
    assertThat(job()).hasRetries(0).hasExceptionMessage();
    assertEquals(job().getExceptionMessage(), MyDelegate.JOB_FAILED_MESSAGE);
    assertThat(pi).isWaitingAt("DoSomethingTask");


    //TODO add back when handler registration works
/*
    assertThat(pi).isWaitingAt("HandleIncidentInSubProcessTask");
    complete(task());
    assertThat(pi).isWaitingAt("DoSomethingTask");
    execute(job());
    assertThat(pi).isWaitingAt("CheckTask");*/
  }


}
