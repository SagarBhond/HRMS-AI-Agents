package com.shrija.attendance.a2a;

import com.shrija.attendance.config.AttendanceAiProperties;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class EmployeeAgentClient {

  private final AttendanceAiProperties properties;
  private final A2AAgentClientSupport support;

  public EmployeeAgentClient(AttendanceAiProperties properties, A2AAgentClientSupport support) {
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
