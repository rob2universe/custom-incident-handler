package com.camunda.example.service;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.context.ProcessEngineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InnerJobRetryService {

  ManagementService managementService;
  RuntimeService runtimeService;

  public InnerJobRetryService(ManagementService managementService, RuntimeService runtimeService) {
    this.managementService = managementService;
    this.runtimeService = runtimeService;
  }

  @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW, rollbackFor = {Throwable.class})
  public void retryJob(String jobId, String processInstanceId, String errorDataValue) {

    try {
      // use new Engine context to avoid inner transaction polluting current engine context (changes would be committed)
      ProcessEngineContext.requiresNew();
      // setting data only to stop delegate from simulating error. Real-world service would not require this
      runtimeService.setVariable(processInstanceId, "error", errorDataValue);
      managementService.executeJob(jobId);
    } finally {
      ProcessEngineContext.clear();
    }
  }
}
