package com.hrms.mcpserver.tools;

import com.hrms.mcpserver.domain.LeaveBalance;
import com.hrms.mcpserver.domain.LeaveRequest;
import com.hrms.mcpserver.repository.LeaveBalanceRepository;
import com.hrms.mcpserver.repository.LeaveRequestRepository;
import com.hrms.mcpserver.repository.EmployeeRepository;
import com.hrms.mcpserver.domain.Employee;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Tools backing the Leave Agent — "Leave requests and balances". Called by the Manager Agent
 * (approvals), HR Agent (lifecycle checks) and Payroll Agent (unpaid-leave deductions), per the
 * responsibility table.
 */
@Component
public class LeaveTools {

  private final LeaveRequestRepository leaveRequestRepository;
  private final LeaveBalanceRepository leaveBalanceRepository;
  private final EmployeeRepository employeeRepository;

  public LeaveTools(
      LeaveRequestRepository leaveRequestRepository,
      LeaveBalanceRepository leaveBalanceRepository,
      EmployeeRepository employeeRepository) {
    this.leaveRequestRepository = leaveRequestRepository;
    this.leaveBalanceRepository = leaveBalanceRepository;
    this.employeeRepository = employeeRepository;
  }

  @Tool(description = "Submit a new leave request for an employee")
  public LeaveRequest applyForLeave(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Leave type: SICK, CASUAL, EARNED, UNPAID, MATERNITY, PATERNITY")
          LeaveRequest.LeaveType leaveType,
      @ToolParam(description = "Start date (yyyy-MM-dd)") LocalDate startDate,
      @ToolParam(description = "End date (yyyy-MM-dd)") LocalDate endDate,
      @ToolParam(description = "Reason for leave") String reason) {
    LeaveRequest request =
        LeaveRequest.builder()
            .employeeId(employeeId)
            .leaveType(leaveType)
            .startDate(startDate)
            .endDate(endDate)
            .status(LeaveRequest.LeaveStatus.PENDING)
            .reason(reason)
            .build();
    return leaveRequestRepository.save(request);
  }

  @Tool(description = "Approve or reject a pending leave request (used by the Manager Agent)")
  public LeaveRequest decideOnLeaveRequest(
      @ToolParam(description = "Leave request ID") Long leaveRequestId,
      @ToolParam(description = "true to approve, false to reject") boolean approve,
      @ToolParam(description = "Manager employee ID making the decision")
          String managerEmployeeId) {
    LeaveRequest request =
        leaveRequestRepository
            .findById(leaveRequestId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "No leave request found with id " + leaveRequestId));
    request.setStatus(
        approve ? LeaveRequest.LeaveStatus.APPROVED : LeaveRequest.LeaveStatus.REJECTED);
    request.setApprovedByManagerId(managerEmployeeId);
    LeaveRequest saved = leaveRequestRepository.save(request);
    if (approve) {
      applyLeaveToBalance(request);
    }
    return saved;
  }

  private void applyLeaveToBalance(LeaveRequest request) {
    int year = request.getStartDate().getYear();
    double days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
    LeaveBalance balance =
        leaveBalanceRepository
            .findByEmployeeIdAndLeaveTypeAndYear(
                request.getEmployeeId(), request.getLeaveType(), year)
            .orElseGet(
                () ->
                    LeaveBalance.builder()
                        .employeeId(request.getEmployeeId())
                        .leaveType(request.getLeaveType())
                        .year(year)
                        .totalDays(0)
                        .usedDays(0)
                        .remainingDays(0)
                        .build());
    balance.setUsedDays(balance.getUsedDays() + days);
    balance.setRemainingDays(balance.getTotalDays() - balance.getUsedDays());
    leaveBalanceRepository.save(balance);
  }

  @Tool(description = "Get all leave requests for an employee, optionally filtered by status")
  public List<LeaveRequest> getLeaveRequests(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(
              description =
                  "Status filter: PENDING, APPROVED, REJECTED, CANCELLED, or null for all")
          LeaveRequest.LeaveStatus status) {
    if (status == null) return leaveRequestRepository.findByEmployeeId(employeeId);
    return leaveRequestRepository.findByEmployeeIdAndStatus(employeeId, status);
  }

  @Tool(
      description = "Get an employee's leave balances for a given year (defaults to current year)")
  public List<LeaveBalance> getLeaveBalance(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Year, or null for current year") Integer year) {
    int y = (year != null) ? year : Year.now().getValue();
    return leaveBalanceRepository.findByEmployeeIdAndYear(employeeId, y);
  }

  @Tool(
      description =
          "Get total unpaid-leave days taken by an employee in a given month/year (used by the Payroll Agent for salary deductions)")
  public double getUnpaidLeaveDaysForMonth(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Month (1-12)") int month,
      @ToolParam(description = "Year") int year) {
    return leaveRequestRepository
        .findByEmployeeIdAndStatus(employeeId, LeaveRequest.LeaveStatus.APPROVED)
        .stream()
        .filter(r -> r.getLeaveType() == LeaveRequest.LeaveType.UNPAID)
        .filter(r -> overlapsMonth(r, month, year))
        .mapToDouble(r -> ChronoUnit.DAYS.between(r.getStartDate(), r.getEndDate()) + 1)
        .sum();
  }

  private boolean overlapsMonth(LeaveRequest r, int month, int year) {
    LocalDate monthStart = LocalDate.of(year, month, 1);
    LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
    return !r.getStartDate().isAfter(monthEnd) && !r.getEndDate().isBefore(monthStart);
  }
  @Tool(description = "Get an employee's leave balances for a year")
  public List<LeaveBalance> getLeaveBalances(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Year or null for current year") Integer year) {
    return getLeaveBalance(employeeId, year);
  }

  @Tool(description = "Cancel a pending or approved leave request")
  public LeaveRequest cancelLeave(@ToolParam(description = "Leave request ID") Long leaveRequestId) {
    LeaveRequest request = leaveRequestRepository.findById(leaveRequestId)
        .orElseThrow(() -> new IllegalArgumentException("No leave request found with id " + leaveRequestId));
    if (request.getStatus() == LeaveRequest.LeaveStatus.CANCELLED) return request;
    if (request.getStatus() == LeaveRequest.LeaveStatus.REJECTED)
      throw new IllegalStateException("Rejected leave cannot be cancelled");
    request.setStatus(LeaveRequest.LeaveStatus.CANCELLED);
    return leaveRequestRepository.save(request);
  }

  @Tool(description = "Modify a leave request before it is finally completed")
  public LeaveRequest modifyLeaveRequest(
      @ToolParam(description = "Leave request ID") Long leaveRequestId,
      @ToolParam(description = "New start date or null") LocalDate startDate,
      @ToolParam(description = "New end date or null") LocalDate endDate,
      @ToolParam(description = "New reason or null") String reason) {
    LeaveRequest request = leaveRequestRepository.findById(leaveRequestId)
        .orElseThrow(() -> new IllegalArgumentException("No leave request found with id " + leaveRequestId));
    if (request.getStatus() != LeaveRequest.LeaveStatus.PENDING)
      throw new IllegalStateException("Only pending leave requests can be modified");
    if (startDate != null) request.setStartDate(startDate);
    if (endDate != null) request.setEndDate(endDate);
    if (reason != null) request.setReason(reason);
    validateDates(request.getStartDate(), request.getEndDate());
    return leaveRequestRepository.save(request);
  }

  @Tool(description = "Get complete leave history for an employee")
  public List<LeaveRequest> getLeaveHistory(@ToolParam(description = "Employee ID") Long employeeId) {
    return leaveRequestRepository.findByEmployeeId(employeeId);
  }

  @Tool(description = "Check whether an employee is eligible for a leave type and duration")
  public EligibilityResult checkLeaveEligibility(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Leave type") LeaveRequest.LeaveType leaveType,
      @ToolParam(description = "Start date") LocalDate startDate,
      @ToolParam(description = "End date") LocalDate endDate) {
    double days = calculateLeaveDuration(employeeId, startDate, endDate);
    if (days <= 0) return new EligibilityResult(false, "End date must not precede start date", days);
    if (leaveType == LeaveRequest.LeaveType.UNPAID) return new EligibilityResult(true, "Unpaid leave is eligible subject to approval", days);
    LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYear(
        employeeId, leaveType, startDate.getYear()).orElse(null);
    if (balance == null) return new EligibilityResult(false, "No leave balance configured for this leave type", days);
    return new EligibilityResult(balance.getRemainingDays() >= days,
        balance.getRemainingDays() >= days ? "Sufficient leave balance" : "Insufficient leave balance", days);
  }

  @Tool(description = "Check overlap with existing leave requests")
  public List<LeaveRequest> checkLeaveOverlap(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Start date") LocalDate startDate,
      @ToolParam(description = "End date") LocalDate endDate) {
    validateDates(startDate, endDate);
    return leaveRequestRepository.findByEmployeeId(employeeId).stream()
        .filter(r -> r.getStatus() != LeaveRequest.LeaveStatus.CANCELLED
            && r.getStatus() != LeaveRequest.LeaveStatus.REJECTED
            && !startDate.isAfter(r.getEndDate()) && !endDate.isBefore(r.getStartDate()))
        .toList();
  }

  @Tool(description = "Calculate inclusive leave duration in days")
  public double calculateLeaveDuration(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Start date") LocalDate startDate,
      @ToolParam(description = "End date") LocalDate endDate) {
    getEmployeeExists(employeeId);
    validateDates(startDate, endDate);
    return ChronoUnit.DAYS.between(startDate, endDate) + 1;
  }

  @Tool(description = "Check whether leave conflicts with weekends")
  public ConflictResult checkWeekendConflict(
      @ToolParam(description = "Start date") LocalDate startDate,
      @ToolParam(description = "End date") LocalDate endDate) {
    validateDates(startDate, endDate);
    long weekendDays = startDate.datesUntil(endDate.plusDays(1))
        .filter(d -> d.getDayOfWeek().getValue() >= 6).count();
    return new ConflictResult(weekendDays > 0, weekendDays, "Weekend dates detected");
  }

  @Tool(description = "Check holiday conflict. The current service has no holiday table, so caller-supplied holiday dates are evaluated.")
  public ConflictResult checkHolidayConflict(
      @ToolParam(description = "Start date") LocalDate startDate,
      @ToolParam(description = "End date") LocalDate endDate,
      @ToolParam(description = "Comma-separated holiday dates yyyy-MM-dd") String holidayDates) {
    validateDates(startDate, endDate);
    long count = java.util.Arrays.stream(holidayDates == null ? new String[0] : holidayDates.split(","))
        .map(String::trim).filter(s -> !s.isBlank()).map(LocalDate::parse)
        .filter(d -> !d.isBefore(startDate) && !d.isAfter(endDate)).count();
    return new ConflictResult(count > 0, count, "Holiday dates detected");
  }

  @Tool(description = "Get an employee leave calendar")
  public List<LeaveRequest> getLeaveCalendar(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Start date") LocalDate startDate,
      @ToolParam(description = "End date") LocalDate endDate) {
    return getLeaveRequests(employeeId, null).stream()
        .filter(r -> !r.getStartDate().isAfter(endDate) && !r.getEndDate().isBefore(startDate)).toList();
  }

  @Tool(description = "Get leave calendar for a manager's team")
  public List<LeaveRequest> getTeamLeaveCalendar(
      @ToolParam(description = "Manager employee ID") Long managerEmployeeId,
      @ToolParam(description = "Start date") LocalDate startDate,
      @ToolParam(description = "End date") LocalDate endDate) {
    List<Long> teamIds = employeeRepository.findByManagerEmployeeId(managerEmployeeId).stream()
        .map(Employee::getEmployeeId).toList();
    return leaveRequestRepository.findAll().stream()
        .filter(r -> teamIds.contains(r.getEmployeeId()))
        .filter(r -> !r.getStartDate().isAfter(endDate) && !r.getEndDate().isBefore(startDate))
        .toList();
  }

  private void validateDates(LocalDate start, LocalDate end) {
    if (start == null || end == null || end.isBefore(start))
      throw new IllegalArgumentException("Invalid leave date range");
  }

  private void getEmployeeExists(Long employeeId) {
    if (employeeId == null) throw new IllegalArgumentException("Employee ID is required");
  }

  public record EligibilityResult(boolean eligible, String reason, double requestedDays) {}
  public record ConflictResult(boolean conflict, long matchingDays, String message) {}

}
