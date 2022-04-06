package com.camunda.example;

import com.camunda.example.service.InnerJobRetryService;
import com.camunda.example.service.MyDelegate;
import com.camunda.example.service.RetryDelegate;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@Deployment(resources = {"process.bpmn", "incident-handling.bpmn"})
public class CustomIncidentHandlerTest {

  public static final String JOB_FAILED_MESSAGE = "Error! jobFailed";

  @Rule
  public ProcessEngineRule engine = new ProcessEngineRule();

  @Before
  public void setUp() {
    Mocks.register("myDelegate", new MyDelegate());
    Mocks.register("retryDelegate", new RetryDelegate(new InnerJobRetryService(managementService(),runtimeService())));
  }

  @Test
  public void testMyFailedJobHandler() {
    var pi = runtimeService().startProcessInstanceByKey("error-process",
        "MY_BUSINESS_KEY1",
        withVariables(MyDelegate.ERROR_DATA, "jobFailed"));
    assertThat(pi).isWaitingAt("DoSomethingTask");
    try {
      execute(job());
    } catch (RuntimeException ex) {
      log.info("Exception with Message: {}", ex.getMessage());
      assertEquals(ex.getMessage(), JOB_FAILED_MESSAGE);
    }
    // 1 retry defined
    try {
      execute(job());
    } catch (RuntimeException ex) {
      log.debug("caught RuntimeException");
    }
    assertThat(job()).hasRetries(0).hasExceptionMessage();
    assertEquals(job().getExceptionMessage(), JOB_FAILED_MESSAGE);
    assertThat(pi).isWaitingAt("DoSomethingTask");

    //restart after issue ahs been resolved
    var ehPi = processInstanceQuery().processDefinitionKey("IncidentHandlingProcess").singleResult();
    assertThat(ehPi).isWaitingAt("HandleIncidentTask");
    complete(task("HandleIncidentTask"), Map.of("error","resolved"));
//    execute(job());
    assertThat(ehPi).hasPassed("TriggerRetryTask").isEnded();
    assertThat(pi)
        .hasPassed("DoSomethingTask")
        .isWaitingAt("CheckTask")
        .variables().containsEntry("error","resolved");
  }


  @Test
  public void testMyFailedJobHandlerRepeatedFailure() {
    var pi = runtimeService().startProcessInstanceByKey("error-process",
        "MY_BUSINESS_KEY1",
        withVariables(MyDelegate.ERROR_DATA, "jobFailed"));
    assertThat(pi).isWaitingAt("DoSomethingTask");
    try {
      execute(job());
    } catch (RuntimeException ex) {
      log.info("Exception with Message: {}", ex.getMessage());
      assertEquals(ex.getMessage(), JOB_FAILED_MESSAGE);
    }
    try {
      execute(job());
    } catch (RuntimeException ex) {
      log.debug("caught RuntimeException");
    }
    assertThat(job()).hasRetries(0).hasExceptionMessage();
    assertEquals(job().getExceptionMessage(), JOB_FAILED_MESSAGE);
    assertThat(pi).isWaitingAt("DoSomethingTask");

    //restart after issue ahs been resolved
    var ehPi = processInstanceQuery().processDefinitionKey("IncidentHandlingProcess").singleResult();
    assertThat(ehPi).isWaitingAt("HandleIncidentTask");
    complete(task("HandleIncidentTask"), Map.of("error","still failing"));
//    execute(job());
    assertThat(pi)
        .isNotWaitingAt("CheckTask")
        .isWaitingAt("DoSomethingTask");
  }

  @Test
  public void testMyFailedJobHandlerWithoutExtensionProperties() {
    var pi = runtimeService().createProcessInstanceByKey("error-process")
            .businessKey("MY_BUSINESS_KEY2")
            .setVariables(withVariables(MyDelegate.ERROR_DATA, "jobFailed"))
            .startBeforeActivity("DoSomethingFailingWithoutExtensionPropertiesTask")
            .execute();
    assertThat(pi).isWaitingAt("DoSomethingFailingWithoutExtensionPropertiesTask");
    try {
      execute(job());
    } catch (RuntimeException ex) {
      log.info("Exception with Message: {}", ex.getMessage());
      assertEquals(ex.getMessage(), JOB_FAILED_MESSAGE);
    }
    // 1 retry defined
    try {
      execute(job());
    } catch (RuntimeException ex) {
      log.debug("caught RuntimeException");
    }
    assertThat(job()).hasRetries(0).hasExceptionMessage();
    assertEquals(job().getExceptionMessage(), JOB_FAILED_MESSAGE);
    assertThat(pi).isWaitingAt("DoSomethingFailingWithoutExtensionPropertiesTask");

  }
}
