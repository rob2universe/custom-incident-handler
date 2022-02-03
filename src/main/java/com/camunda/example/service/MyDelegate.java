package com.camunda.example.service;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MyDelegate implements JavaDelegate {

  public static final String BPMN_ERROR = "bpmn";
  public static final String ERROR_DATA = "error";

  @Override
  public void execute(DelegateExecution execution) throws RuntimeException {

    String error = (String) execution.getVariable(ERROR_DATA);
    log.info("error data value is {}", error);
    if (error != null && !error.equals("resolved")) {
      if (error.equals(BPMN_ERROR))
        throw new BpmnError("Processing Failed - BPMN");
      else {
        throw new RuntimeException("Error! " + error);
      }
    }
  }
}
