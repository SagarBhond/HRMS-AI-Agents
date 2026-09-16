package com.shrija.manager.a2a;

import com.shrija.manager.config.ManagerAiProperties;
import org.springframework.stereotype.Component;

@Component
public class AttendanceAgentClient {

  private final ManagerAiProperties properties;
  private final A2AAgentClientSupport support;

  public AttendanceAgentClient(ManagerAiProperties properties, A2AAgentClientSupport support) {
    this.properties = properties;
    this.support = support;
  }

  /** Requests the confirmed team attendance/working-hours summary for a given date. */
  public String getTeamAttendanceReport(String managerEmployeeId, String date) {
    return support.call(
        properties.attendanceAgentUrl(),
        "Manager "
            + managerEmployeeId
            + " is requesting the confirmed team attendance report for "
            + date
            + ". Return only attendance records already confirmed by the Attendance Agent's MCP "
            + "tools. Do not invent or estimate any record.");
  }
}
