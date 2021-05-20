package com.camunda.example.incident;

import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.Incident;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.camunda.example.incident.MyIncidentHandler.INCIDENT_TYPE;

@Configuration
public class IncidentHandlerProcessEnginePlugin extends AbstractProcessEnginePlugin {

    @Override
    public void preInit(ProcessEngineConfigurationImpl engineConfig) {
        engineConfig.setCustomIncidentHandlers(List.of(
                // existing standard incident types
                new MyFailedJobIncidentHandler(Incident.FAILED_JOB_HANDLER_TYPE),
                new MyFailedJobIncidentHandler(Incident.EXTERNAL_TASK_HANDLER_TYPE),
                // custom incident type
                new MyIncidentHandler(INCIDENT_TYPE))
        );
    }
}

