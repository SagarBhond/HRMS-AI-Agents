package com.shrija.manager.a2a;

import com.shrija.manager.config.ManagerAiProperties;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LeaveAgentClient {

  private final ManagerAiProperties properties;
  private final A2AAgentClientSupport support;

  public LeaveAgentClient(ManagerAiProperties properties, A2AAgentClientSupport support) {
    this.properties = properties;
    this.support = support;
  }

  /** Pulls the confirmed, currently-pending leave requests for this manager's team. */
  public String getPendingApprovals(String managerEmployeeId) {
    return support.call(
        properties.leaveAgentUrl(),
        "Manager "
            + managerEmployeeId
            + " is requesting the list of currently PENDING leave requests for their direct "
            + "reports. Return only confirmed pending requests with their leaveRequestId, "
            + "employeeId, leaveType, startDate and endDate. Do not invent any request.");
  }

  /** Records the manager's approve/reject decision against a specific leave request. */
  public Map<String, Object> decideOnLeaveRequest(
      String managerEmployeeId, Long leaveRequestId, boolean approve) {
    String response =
        support.call(
            properties.leaveAgentUrl(),
            "Manager "
                + managerEmployeeId
                + " is deciding on leave request "
                + leaveRequestId
                + ". Decision: "
                + (approve ? "APPROVE" : "REJECT")
                + ". Apply this decision now and confirm the resulting status.");
    return Map.of(
        "leaveRequestId", leaveRequestId,
        "decision", approve ? "APPROVED" : "REJECTED",
        "leaveAgentResponse", response);
  }
}
