package com.shrija.attendance.a2a;

import com.shrija.attendance.config.AttendanceAiProperties;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PayrollAgentClient {

  private final AttendanceAiProperties properties;
  private final A2AAgentClientSupport support;

  public PayrollAgentClient(AttendanceAiProperties properties, A2AAgentClientSupport support) {
    this.properties = properties;
    this.support = support;
  }

  public Map<String, Object> sendAttendanceSummary(
      String employeeId, String month, Object summary) {
    String response =
        support.call(
            properties.payrollAgentUrl(),
            "Attendance Agent is providing confirmed attendance data for employee "
                + employeeId
                + " for "
                + month
                + ". Use this data for payroll only; do not modify attendance. "
                + "Confirmed summary: "
                + summary);
    return Map.of(
        "sent", true, "employeeId", employeeId, "month", month, "payrollAgentResponse", response);
  }
}
