package com.camunda.example.service;

import com.camunda.example.dto.ErrorForIncident;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RetryDelegate implements JavaDelegate {

  InnerJobRetryService innerJobRetryService;

  public RetryDelegate(InnerJobRetryService innerJobRetryService) {
    this.innerJobRetryService = innerJobRetryService;
  }

  @Override
  public void execute(DelegateExecution exec) throws BpmnError {

    ObjectValue efiObj = exec.getVariableTyped("errorForIncident");
    ErrorForIncident efi = efiObj.getValue(ErrorForIncident.class);
    log.info("ErrorForIncident objectValue: {}", efi);

    try {
      log.info("Retrying failed job with id: {} for process instance id {}", efi.getJobId(), efi.getProcessInstance());
      innerJobRetryService.retryJob(efi.getJobId(), efi.getProcessInstance(), (String) exec.getVariable("error"));

    } catch (RuntimeException e) {
      log.info("Retrying failed job with id {} for process instance {} failed again with error: {}", efi.getJobId(), exec.getProcessInstance().getId(), e.getMessage());
      throw new BpmnError("100", "Retry did not resolve the incident" + e.getMessage());
    }
  }
}

