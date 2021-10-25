package com.camunda.example.service;

import com.camunda.example.dto.ErrorForIncident;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RetryDelegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution exec) {

    ObjectValue efiObj = exec.getVariableTyped("errorForIncident");
    ErrorForIncident efi = efiObj.getValue(ErrorForIncident.class);
    log.info("ErrorForIncident objectValue: {}", efi);

    exec.getProcessEngineServices().getRuntimeService()
        // setting data only to stop delegate from simulating error
        .setVariable(efi.getProcessInstance(), "error", "resolved");
    exec.getProcessEngineServices().getManagementService()
        .setJobRetriesByJobDefinitionId(efi.getJobId(), 1);
  }
}
