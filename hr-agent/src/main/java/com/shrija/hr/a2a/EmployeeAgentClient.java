package com.shrija.hr.a2a;

import com.shrija.hr.config.HrAiProperties;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class EmployeeAgentClient {

  private final HrAiProperties properties;
  private final A2AAgentClientSupport support;

  public EmployeeAgentClient(HrAiProperties properties, A2AAgentClientSupport support) {
    this.properties = properties;
    this.support = support;
  }

  public Map<String, Object> getEmployeeProfile(String employeeId) {

    String response =
        support.call(
            properties.employeeAgentUrl(),
            "Leave Agent needs to verify employee "
                + employeeId
                + ". Retrieve the employee profile and confirm whether this employee exists "
                + "and is currently eligible to use the leave system.");

    return Map.of(
        "sent", true,
        "employeeId", employeeId,
        "employeeAgentResponse", response);
  }
}
