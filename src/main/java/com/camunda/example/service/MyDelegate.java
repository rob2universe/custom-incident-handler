package com.camunda.example.service;

import com.camunda.example.incident.MyIncidentHandler;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MyDelegate implements JavaDelegate {

    public static final String BPMN_ERROR = "bpmn";
    public static final String FAILED_JOB = "failedjob";
    public static final String MY_INCIDENT = "myincident";
    public static final String ERROR_DATA = "error";
    public static final String MY_INCIDENT_MESSAGE = "MY_INCIDENT_MESSAGE!!!";
    public static final String JOB_FAILED_MESSAGE = "Processing Failed - Incident";

    @Override
    public void execute(DelegateExecution execution) throws RuntimeException {

        String error = (String) execution.getVariable(ERROR_DATA);
        log.info("error data value is {}", error);
        if (error != null) {
            if (error.equals(BPMN_ERROR))
                throw new BpmnError("Processing Failed - BPMN");
            if (error.equals(FAILED_JOB))
                throw new RuntimeException(JOB_FAILED_MESSAGE);
            if (error.equals(MY_INCIDENT))
                execution.createIncident(MyIncidentHandler.INCIDENT_TYPE, execution.getId(), MY_INCIDENT_MESSAGE);
            throw new RuntimeException("Created incident from delegate");
        }

    }
}
