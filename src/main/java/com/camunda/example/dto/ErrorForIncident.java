package com.camunda.example.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ErrorForIncident implements Serializable {
  private String processInstance;
  private String activityId;
  private String errorMessage;
  private String jobId;

}
