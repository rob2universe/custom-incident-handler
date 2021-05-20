package com.camunda.example.incident;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.impl.incident.DefaultIncidentHandler;
import org.camunda.bpm.engine.impl.incident.IncidentContext;
import org.camunda.bpm.engine.impl.incident.IncidentHandler;
import org.camunda.bpm.engine.runtime.Incident;

@Slf4j
public class MyExternalTaskIncidentHandler extends DefaultIncidentHandler implements IncidentHandler {

    public MyExternalTaskIncidentHandler(String type) {
        super(type);
    }

    @Override
    public Incident handleIncident(IncidentContext context, String message) {
        log.info("MY_FAILED_EXTERNAL_TASK_INCIDENT!!!");
        return super.handleIncident(context, message);
    }

}
