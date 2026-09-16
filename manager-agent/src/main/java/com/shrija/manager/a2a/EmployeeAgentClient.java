package com.shrija.manager.a2a;

import com.shrija.manager.config.ManagerAiProperties;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class EmployeeAgentClient {

  private final ManagerAiProperties properties;
  private final A2AAgentClientSupport support;

  public EmployeeAgentClient(ManagerAiProperties properties, A2AAgentClientSupport support) {
    this.properties = properties;
    this.support = support;
  }

  public Map<String, Object> verifyEmployee(String employeeId) {
    String response =
        support.call(
            properties.employeeAgentUrl(),
            "Verify employee "
                + employeeId
                + ". Return only confirmed employee identity information. "
                + "If the employee does not exist, clearly say NOT_FOUND.");
    if (response.contains("NOT_FOUND")) {
      return Map.of("verified", false, "employeeId", employeeId, "message", response);
    }
    return Map.of("verified", true, "employeeId", employeeId, "employeeAgentResponse", response);
  }
}
