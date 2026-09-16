package com.shrija.hr.a2a;

import com.shrija.hr.config.HrAiProperties;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ManagerAgentClient {

    private final HrAiProperties properties;
    private final A2AAgentClientSupport support;

    public ManagerAgentClient(
            HrAiProperties properties,
            A2AAgentClientSupport support) {
        this.properties = properties;
        this.support = support;
    }

    public Map<String, Object> sendLeaveRequestToManager(
            String employeeId,
            String leaveRequestId,
            String leaveType,
            String startDate,
            String endDate,
            String reason) {

        String response =
                support.call(
                        properties.managerAgentUrl(),
                        "Leave Agent is sending leave request "
                                + leaveRequestId
                                + " for employee "
                                + employeeId
                                + ". Leave type: "
                                + leaveType
                                + ". Start date: "
                                + startDate
                                + ". End date: "
                                + endDate
                                + ". Reason: "
                                + reason
                                + ". Review the request and provide the manager approval/rejection decision.");

        return Map.of(
                "sent", true,
                "employeeId", employeeId,
                "leaveRequestId", leaveRequestId,
                "managerAgentResponse", response);
    }

}
