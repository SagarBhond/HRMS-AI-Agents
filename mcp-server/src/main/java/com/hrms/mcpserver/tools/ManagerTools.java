package com.hrms.mcpserver.tools;

import com.hrms.mcpserver.domain.Employee;
import com.hrms.mcpserver.domain.LeaveRequest;
import com.hrms.mcpserver.repository.EmployeeRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Tools backing the Manager Agent — People Manager operations only (team roster, approvals,
 * team-level reporting, escalation). Manager functions are thin, read-mostly aggregations over
 * Employee/Attendance/Leave/Payroll/Performance data; the underlying leave decision itself is
 * made via LeaveTools#decideOnLeaveRequest, which the Manager Agent is separately allow-listed to
 * call.
 */
@Component
public class ManagerTools {

  private final EmployeeRepository employeeRepository;
  private final AttendanceTools attendanceTools;
  private final LeaveTools leaveTools;
  private final PayrollTools payrollTools;
  private final PerformanceTools performanceTools;
  private final NotificationTools notificationTools;

  public ManagerTools(
      EmployeeRepository employeeRepository,
      AttendanceTools attendanceTools,
      LeaveTools leaveTools,
      PayrollTools payrollTools,
      PerformanceTools performanceTools,
      NotificationTools notificationTools) {
    this.employeeRepository = employeeRepository;
    this.attendanceTools = attendanceTools;
    this.leaveTools = leaveTools;
    this.payrollTools = payrollTools;
    this.performanceTools = performanceTools;
    this.notificationTools = notificationTools;
  }

  @Tool(description = "Get the list of employees reporting to a manager")
  public List<Employee> getTeamMembers(@ToolParam(description = "Manager employee ID") Long managerEmployeeId) {
    return employeeRepository.findByManagerEmployeeId(managerEmployeeId);
  }

  @Tool(description = "Get a specific team member's profile, scoped to the manager's team")
  public Employee getTeamMemberProfile(
      @ToolParam(description = "Manager employee ID") Long managerEmployeeId,
      @ToolParam(description = "Team member employee ID") Long employeeId) {
    return getTeamMembers(managerEmployeeId).stream()
        .filter(e -> e.getEmployeeId().equals(employeeId))
        .findFirst()
        .orElseThrow(
            () -> new IllegalArgumentException(
                "Employee " + employeeId + " does not report to manager " + managerEmployeeId));
  }

  @Tool(description = "Get all pending leave requests awaiting this manager's decision")
  public List<LeaveRequest> getPendingApprovals(@ToolParam(description = "Manager employee ID") Long managerEmployeeId) {
    return getTeamMembers(managerEmployeeId).stream()
        .flatMap(e -> leaveTools.getLeaveRequests(e.getEmployeeId(), LeaveRequest.LeaveStatus.PENDING).stream())
        .toList();
  }

  @Tool(description = "Generate an attendance report for a manager's whole team on a given work date")
  public AttendanceTools.TeamAttendanceReport getTeamAttendanceReport(
      @ToolParam(description = "Manager employee ID") Long managerEmployeeId,
      @ToolParam(description = "Work date (yyyy-MM-dd)") LocalDate workDate) {
    return attendanceTools.generateTeamAttendanceReport(managerEmployeeId, workDate);
  }

  @Tool(description = "Escalate an employee issue from a manager to HR")
  public NotificationTools.NotificationRecord escalateToHr(
      @ToolParam(description = "Manager employee ID raising the escalation") Long managerEmployeeId,
      @ToolParam(description = "Employee ID the escalation concerns") Long employeeId,
      @ToolParam(description = "Reason for escalation") String reason) {
    return notificationTools.sendHrNotification(
        "Escalation from manager " + managerEmployeeId,
        "Regarding employee " + employeeId + ": " + reason);
  }

  @Tool(
      description =
          "Get a one-shot team overview for a manager: headcount, pending leave, and today's attendance")
  public TeamOverview getTeamOverview(@ToolParam(description = "Manager employee ID") Long managerEmployeeId) {
    long headcount = getTeamHeadcount(managerEmployeeId);
    int pendingLeave = getPendingApprovals(managerEmployeeId).size();
    List<com.hrms.mcpserver.domain.Attendance> today =
        attendanceTools.getTeamAttendance(managerEmployeeId, LocalDate.now());
    long present =
        today.stream()
            .filter(
                a ->
                    a.getStatus() == com.hrms.mcpserver.domain.Attendance.AttendanceStatus.PRESENT
                        || a.getStatus() == com.hrms.mcpserver.domain.Attendance.AttendanceStatus.HALF_DAY)
            .count();
    return new TeamOverview(managerEmployeeId, headcount, pendingLeave, present, headcount - present);
  }

  @Tool(description = "Get a summary of the manager's team leave: pending, approved, and on-leave-today counts")
  public TeamLeaveSummary getTeamLeaveSummary(@ToolParam(description = "Manager employee ID") Long managerEmployeeId) {
    List<Employee> team = getTeamMembers(managerEmployeeId);
    long pending = getPendingApprovals(managerEmployeeId).size();
    long approvedTotal =
        team.stream()
            .flatMap(e -> leaveTools.getLeaveRequests(e.getEmployeeId(), LeaveRequest.LeaveStatus.APPROVED).stream())
            .count();
    long onLeaveToday =
        team.stream()
            .flatMap(e -> leaveTools.getLeaveRequests(e.getEmployeeId(), LeaveRequest.LeaveStatus.APPROVED).stream())
            .filter(r -> !r.getStartDate().isAfter(LocalDate.now()) && !r.getEndDate().isBefore(LocalDate.now()))
            .count();
    return new TeamLeaveSummary(managerEmployeeId, pending, approvedTotal, onLeaveToday);
  }

  @Tool(description = "Get an attendance summary (percentage, present/working days) for the manager's whole team for a month")
  public List<AttendanceTools.AttendancePercentage> getTeamAttendanceSummary(
      @ToolParam(description = "Manager employee ID") Long managerEmployeeId,
      @ToolParam(description = "Month 1-12") int month,
      @ToolParam(description = "Year") int year) {
    return getTeamMembers(managerEmployeeId).stream()
        .map(e -> attendanceTools.calculateAttendancePercentage(e.getEmployeeId(), month, year))
        .toList();
  }

  @Tool(description = "Get the manager's total direct-report headcount")
  public long getTeamHeadcount(@ToolParam(description = "Manager employee ID") Long managerEmployeeId) {
    return getTeamMembers(managerEmployeeId).stream()
        .filter(e -> e.getStatus() == Employee.EmployeeStatus.ACTIVE)
        .count();
  }

  @Tool(
      description =
          "Get actionable alerts for a manager's team: below-90% attendance and unusually high leave usage")
  public List<String> getTeamAlerts(@ToolParam(description = "Manager employee ID") Long managerEmployeeId) {
    LocalDate now = LocalDate.now();
    java.util.ArrayList<String> alerts = new java.util.ArrayList<>();
    for (Employee e : getTeamMembers(managerEmployeeId)) {
      AttendanceTools.AttendancePercentage pct =
          attendanceTools.calculateAttendancePercentage(e.getEmployeeId(), now.getMonthValue(), now.getYear());
      if (pct.percentage() < 90) {
        alerts.add(e.getFirstName() + " " + e.getLastName() + " (id " + e.getEmployeeId() + ") attendance is below 90% this month");
      }
    }
    return alerts;
  }

  @Tool(
      description =
          "Get all actions currently pending the manager's attention: leave approvals and team alerts")
  public PendingActions getPendingActions(@ToolParam(description = "Manager employee ID") Long managerEmployeeId) {
    return new PendingActions(getPendingApprovals(managerEmployeeId), getTeamAlerts(managerEmployeeId));
  }

  @Tool(description = "Get a payroll summary across the manager's team for a given month")
  public List<PayrollTools.PayrollSummary> getTeamPayrollSummary(
      @ToolParam(description = "Manager employee ID") Long managerEmployeeId,
      @ToolParam(description = "Month 1-12") int month,
      @ToolParam(description = "Year") int year) {
    return getTeamMembers(managerEmployeeId).stream()
        .flatMap(
            e -> {
              try {
                return java.util.stream.Stream.of(payrollTools.getPayrollSummary(e.getEmployeeId(), month, year));
              } catch (IllegalArgumentException ex) {
                return java.util.stream.Stream.empty();
              }
            })
        .toList();
  }

  @Tool(description = "Get a performance summary across the manager's team")
  public List<PerformanceTools.PerformanceSummary> getTeamPerformanceSummary(
      @ToolParam(description = "Manager employee ID") Long managerEmployeeId) {
    return getTeamMembers(managerEmployeeId).stream()
        .map(e -> performanceTools.generatePerformanceSummary(e.getEmployeeId()))
        .toList();
  }

  public record TeamOverview(
      Long managerEmployeeId, long headcount, int pendingLeaveRequests, long presentToday, long notPresentToday) {}

  public record TeamLeaveSummary(
      Long managerEmployeeId, long pendingRequests, long approvedRequests, long onLeaveToday) {}

  public record PendingActions(List<LeaveRequest> pendingLeaveApprovals, List<String> alerts) {}
}
