package com.shrija.orchestrator.tool;

import com.shrija.orchestrator.a2a.AttendanceAgentClient;
import com.shrija.orchestrator.a2a.EmployeeAgentClient;
import com.shrija.orchestrator.a2a.HrAgentClient;
import com.shrija.orchestrator.a2a.LeaveAgentClient;
import com.shrija.orchestrator.a2a.ManagerAgentClient;
import com.shrija.orchestrator.a2a.PayrollAgentClient;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * These are the tools the Orchestration Agent's LLM calls to perform "ADK sub-agent delegation"
 * over A2A, one per domain agent from the responsibility table. Every method takes the requester's
 * identity/role/target/date as EXPLICIT parameters -- the LLM must copy them character-for-
 * character from the "Authenticated actor: ..." context line the ConversationService injects (never
 * invent them) -- and this class (plain Java, not the LLM) is what actually assembles the trusted
 * context line forwarded to the downstream agent. This mirrors how CheckInTool / ApproveLeaveTool
 * etc. already require requesterEmployeeId/requesterRole as explicit, literally-copied arguments
 * rather than trusting free text.
 */
@Component
public class DelegationTools {

  private final EmployeeAgentClient employeeAgentClient;
  private final AttendanceAgentClient attendanceAgentClient;
  private final PayrollAgentClient payrollAgentClient;
  private final HrAgentClient hrAgentClient;
  private final LeaveAgentClient leaveAgentClient;
  private final ManagerAgentClient managerAgentClient;

  public DelegationTools(
      EmployeeAgentClient employeeAgentClient,
      AttendanceAgentClient attendanceAgentClient,
      PayrollAgentClient payrollAgentClient,
      HrAgentClient hrAgentClient,
      LeaveAgentClient leaveAgentClient,
      ManagerAgentClient managerAgentClient) {
    this.employeeAgentClient = employeeAgentClient;
    this.attendanceAgentClient = attendanceAgentClient;
    this.payrollAgentClient = payrollAgentClient;
    this.hrAgentClient = hrAgentClient;
    this.leaveAgentClient = leaveAgentClient;
    this.managerAgentClient = managerAgentClient;
  }

  public Map<String, Object> delegateToEmployeeAgent(
      String requesterEmployeeId,
      String requesterRole,
      String targetEmployeeId,
      String currentDate,
      String userMessage) {
    return runDelegation(
        "Employee Agent",
        employeeAgentClient::delegate,
        requesterEmployeeId,
        requesterRole,
        targetEmployeeId,
        currentDate,
        userMessage);
  }

  public Map<String, Object> delegateToAttendanceAgent(
      String requesterEmployeeId,
      String requesterRole,
      String targetEmployeeId,
      String currentDate,
      String userMessage) {
    return runDelegation(
        "Attendance Agent",
        attendanceAgentClient::delegate,
        requesterEmployeeId,
        requesterRole,
        targetEmployeeId,
        currentDate,
        userMessage);
  }

  public Map<String, Object> delegateToPayrollAgent(
      String requesterEmployeeId,
      String requesterRole,
      String targetEmployeeId,
      String currentDate,
      String userMessage) {
    return runDelegation(
        "Payroll Agent",
        payrollAgentClient::delegate,
        requesterEmployeeId,
        requesterRole,
        targetEmployeeId,
        currentDate,
        userMessage);
  }

  public Map<String, Object> delegateToHrAgent(
      String requesterEmployeeId,
      String requesterRole,
      String targetEmployeeId,
      String currentDate,
      String userMessage) {
    return runDelegation(
        "HR Agent",
        hrAgentClient::delegate,
        requesterEmployeeId,
        requesterRole,
        targetEmployeeId,
        currentDate,
        userMessage);
  }

  public Map<String, Object> delegateToLeaveAgent(
      String requesterEmployeeId,
      String requesterRole,
      String targetEmployeeId,
      String currentDate,
      String userMessage) {
    return runDelegation(
        "Leave Agent",
        leaveAgentClient::delegate,
        requesterEmployeeId,
        requesterRole,
        targetEmployeeId,
        currentDate,
        userMessage);
  }

  public Map<String, Object> delegateToManagerAgent(
      String requesterEmployeeId,
      String requesterRole,
      String targetEmployeeId,
      String currentDate,
      String userMessage) {
    return runDelegation(
        "Manager Agent",
        managerAgentClient::delegate,
        requesterEmployeeId,
        requesterRole,
        targetEmployeeId,
        currentDate,
        userMessage);
  }

  private Map<String, Object> runDelegation(
      String targetAgentName,
      java.util.function.Function<String, String> delegate,
      String requesterEmployeeId,
      String requesterRole,
      String targetEmployeeId,
      String currentDate,
      String userMessage) {
    if (requesterEmployeeId == null
        || requesterEmployeeId.isBlank()
        || requesterRole == null
        || requesterRole.isBlank()) {
      throw new SecurityException(
          "Requester identity/role must be copied from the Authenticated actor context line.");
    }
    String effectiveTarget =
        (targetEmployeeId == null || targetEmployeeId.isBlank())
            ? requesterEmployeeId
            : targetEmployeeId;
    String groundedMessage =
        "Authenticated actor: "
            + requesterEmployeeId
            + "; role: "
            + requesterRole
            + "; requesterEmployeeId: "
            + requesterEmployeeId
            + "; targetEmployeeId: "
            + effectiveTarget
            + "; currentDate: "
            + currentDate
            + ".\nUser request: "
            + userMessage;
    try {
      String response = delegate.apply(groundedMessage);
      return Map.of("agent", targetAgentName, "response", response);
    } catch (RuntimeException ex) {
      return Map.of(
          "agent",
          targetAgentName,
          "error",
          targetAgentName + " is unavailable: " + ex.getMessage());
    }
  }
}
