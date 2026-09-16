package com.shrija.attendance.a2a;

import com.shrija.attendance.config.AttendanceAiProperties;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OrchestratorAgentClient {

    private final AttendanceAiProperties properties;
    private final A2AAgentClientSupport support;

    public OrchestratorAgentClient(
            AttendanceAiProperties properties,
            A2AAgentClientSupport support) {
        this.properties = properties;
        this.support = support;
    }

    public Map<String, Object> sendAttendanceResult(
            String date,
            Object attendanceResult) {

        String response =
                support.call(
                        properties.orchestratorAgentUrl(),
                        "Attendance Agent is providing confirmed attendance information for "
                                + date
                                + ". Do not invent, modify, or change the records. "
                                + "Confirmed attendance result: "
                                + attendanceResult);

        return Map.of(
                "sent", true,
                "date", date,
                "orchestratorAgentResponse", response);
    }
}
