package com.camunda.example.incident;

import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.impl.incident.DefaultIncidentHandler;
import org.camunda.bpm.engine.impl.incident.IncidentContext;
import org.camunda.bpm.engine.impl.incident.IncidentHandler;
import org.camunda.bpm.engine.runtime.Incident;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MyFailedJobIncidentHandler extends DefaultIncidentHandler implements IncidentHandler {

    public MyFailedJobIncidentHandler(String type) {
        super(type);
    }

    @Getter
    private List<String> activityIds = new ArrayList<>();

    @Override
    public Incident handleIncident(IncidentContext context, String message) {
        activityIds.add(context.getActivityId());
        log.info("MY_FAILED_JOB_INCIDENT!!!");
        return super.handleIncident(context, message);
    }

}
