package com.camunda.example.incident;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.incident.DefaultIncidentHandler;
import org.camunda.bpm.engine.impl.incident.IncidentContext;
import org.camunda.bpm.engine.impl.incident.IncidentHandler;
import org.camunda.bpm.engine.runtime.Incident;

import java.util.Map;

@Slf4j
public class MyIncidentHandler extends DefaultIncidentHandler implements IncidentHandler {

    public static final String INCIDENT_TYPE = "MyIncident";

    public MyIncidentHandler(String type) {
        super(type);
    }

    @Override
    public Incident handleIncident(IncidentContext context, String message) {
        log.info("Incident in activity id {} - Message: {} - Configuration: {} ", context.getActivityId(), message, context.getConfiguration());
        return super.handleIncident(context, message);
    }

}
