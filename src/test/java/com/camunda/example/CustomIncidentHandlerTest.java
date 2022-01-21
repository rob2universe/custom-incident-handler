package com.camunda.example;

import com.camunda.example.service.MyDelegate;
import com.camunda.example.service.RetryDelegate;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@Deployment(resources = {"process.bpmn", "incident-handling.bpmn"})
public class CustomIncidentHandlerTest {

  @Rule
  public ProcessEngineRule engine = new ProcessEngineRule();

  @Before
  public void setUp() {
    Mocks.register("myDelegate", new MyDelegate());
    Mocks.register("retryDelegate", new RetryDelegate());
  }

  @Test
  public void testMyFailedJobHandler() {
    var pi = runtimeService().startProcessInstanceByKey("error-process",
        "MY_BUSINESS_KEY1",
        withVariables(MyDelegate.ERROR_DATA, MyDelegate.FAILED_JOB));
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
      log.debug("caught RuntimeException");
    }
    assertThat(job()).hasRetries(0).hasExceptionMessage();
    assertEquals(job().getExceptionMessage(), MyDelegate.JOB_FAILED_MESSAGE);
    assertThat(pi).isWaitingAt("DoSomethingTask");

    //restart after issue ahs been resolved
    var ehPi = processInstanceQuery().processDefinitionKey("IncidentHandlingProcess").singleResult();
    assertThat(ehPi).isWaitingAt("HandleIncidentTask");
    complete(task("HandleIncidentTask"));
    assertThat(pi).isWaitingAt("DoSomethingTask");
    execute(job());
    assertThat(pi)
        .hasPassed("DoSomethingTask")
        .isWaitingAt("CheckTask")
        .variables().containsEntry("error","resolved");
  }
}
