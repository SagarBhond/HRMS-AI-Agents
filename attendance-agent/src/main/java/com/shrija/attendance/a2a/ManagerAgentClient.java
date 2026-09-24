package com.shrija.attendance.a2a;

import com.shrija.attendance.config.AttendanceAiProperties;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ManagerAgentClient {

  private final AttendanceAiProperties properties;
  private final A2AAgentClientSupport support;

  public ManagerAgentClient(AttendanceAiProperties properties, A2AAgentClientSupport support) {
    this.properties = properties;
    this.support = support;
  }

  public Map<String, Object> sendTeamAttendance(String date, Object teamAttendance) {
    String response =
        support.call(
            properties.managerAgentUrl(),
            "Attendance Agent is providing confirmed team attendance for "
                + date
                + ". Do not invent or change the records. Confirmed records: "
                + teamAttendance);
    return Map.of("sent", true, "date", date, "managerAgentResponse", response);
  }
}
