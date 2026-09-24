//// package com.hrms.mcpserver.tools;
////
//// import com.hrms.mcpserver.domain.LeaveBalance;
//// import com.hrms.mcpserver.domain.LeaveRequest;
//// import com.hrms.mcpserver.repository.LeaveBalanceRepository;
//// import com.hrms.mcpserver.repository.LeaveRequestRepository;
//// import com.hrms.mcpserver.repository.EmployeeRepository;
//// import com.hrms.mcpserver.domain.Employee;
//// import java.time.LocalDate;
//// import java.time.Year;
//// import java.time.temporal.ChronoUnit;
//// import java.util.List;
//// import org.springframework.ai.tool.annotation.Tool;
//// import org.springframework.ai.tool.annotation.ToolParam;
//// import org.springframework.stereotype.Component;
////
///// **
//// * Tools backing the Leave Agent — "Leave requests and balances". Called by the Manager Agent
//// * (approvals), HR Agent (lifecycle checks) and Payroll Agent (unpaid-leave deductions), per the
//// * responsibility table.
//// */
//// @Component
//// public class LeaveTools {
////
////  private final LeaveRequestRepository leaveRequestRepository;
////  private final LeaveBalanceRepository leaveBalanceRepository;
////  private final EmployeeRepository employeeRepository;
////
////  public LeaveTools(
////      LeaveRequestRepository leaveRequestRepository,
////      LeaveBalanceRepository leaveBalanceRepository,
////      EmployeeRepository employeeRepository) {
////    this.leaveRequestRepository = leaveRequestRepository;
////    this.leaveBalanceRepository = leaveBalanceRepository;
////    this.employeeRepository = employeeRepository;
////  }
////
////  @Tool(description = "Submit a new leave request for an employee")
////  public LeaveRequest applyForLeave(
////      @ToolParam(description = "Employee ID") Long employeeId,
////      @ToolParam(description = "Leave type: SICK, CASUAL, EARNED, UNPAID, MATERNITY, PATERNITY")
////          LeaveRequest.LeaveType leaveType,
////      @ToolParam(description = "Start date (yyyy-MM-dd)") LocalDate startDate,
////      @ToolParam(description = "End date (yyyy-MM-dd)") LocalDate endDate,
////      @ToolParam(description = "Reason for leave") String reason) {
////    LeaveRequest request =
////        LeaveRequest.builder()
////            .employeeId(employeeId)
////            .leaveType(leaveType)
////            .startDate(startDate)
////            .endDate(endDate)
////            .status(LeaveRequest.LeaveStatus.PENDING)
////            .reason(reason)
////            .build();
////    return leaveRequestRepository.save(request);
////  }
////
////  @Tool(description = "Approve or reject a pending leave request (used by the Manager Agent)")
////  public LeaveRequest decideOnLeaveRequest(
////      @ToolParam(description = "Leave request ID") Long leaveRequestId,
////      @ToolParam(description = "true to approve, false to reject") boolean approve,
////      @ToolParam(description = "Manager employee ID making the decision")
////          String managerEmployeeId) {
////    LeaveRequest request =
////        leaveRequestRepository
////            .findById(leaveRequestId)
////            .orElseThrow(
////                () ->
////                    new IllegalArgumentException(
////                        "No leave request found with id " + leaveRequestId));
////    request.setStatus(
////        approve ? LeaveRequest.LeaveStatus.APPROVED : LeaveRequest.LeaveStatus.REJECTED);
////    request.setApprovedByManagerId(managerEmployeeId);
////    LeaveRequest saved = leaveRequestRepository.save(request);
////    if (approve) {
////      applyLeaveToBalance(request);
////    }
////    return saved;
////  }
////
////  private void applyLeaveToBalance(LeaveRequest request) {
////    int year = request.getStartDate().getYear();
////    double days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
////    LeaveBalance balance =
////        leaveBalanceRepository
////            .findByEmployeeIdAndLeaveTypeAndYear(
////                request.getEmployeeId(), request.getLeaveType(), year)
////            .orElseGet(
////                () ->
////                    LeaveBalance.builder()
////                        .employeeId(request.getEmployeeId())
////                        .leaveType(request.getLeaveType())
////                        .year(year)
////                        .totalDays(0)
////                        .usedDays(0)
////                        .remainingDays(0)
////                        .build());
////    balance.setUsedDays(balance.getUsedDays() + days);
////    balance.setRemainingDays(balance.getTotalDays() - balance.getUsedDays());
////    leaveBalanceRepository.save(balance);
////  }
////
////  @Tool(description = "Get all leave requests for an employee, optionally filtered by status")
////  public List<LeaveRequest> getLeaveRequests(
////      @ToolParam(description = "Employee ID") Long employeeId,
////      @ToolParam(
////              description =
////                  "Status filter: PENDING, APPROVED, REJECTED, CANCELLED, or null for all")
////          LeaveRequest.LeaveStatus status) {
////    if (status == null) return leaveRequestRepository.findByEmployeeId(employeeId);
////    return leaveRequestRepository.findByEmployeeIdAndStatus(employeeId, status);
////  }
////
////  @Tool(
////      description = "Get an employee's leave balances for a given year (defaults to current
// year)")
////  public List<LeaveBalance> getLeaveBalance(
////      @ToolParam(description = "Employee ID") Long employeeId,
////      @ToolParam(description = "Year, or null for current year") Integer year) {
////    int y = (year != null) ? year : Year.now().getValue();
////    return leaveBalanceRepository.findByEmployeeIdAndYear(employeeId, y);
////  }
////
////  @Tool(
////      description =
////          "Get total unpaid-leave days taken by an employee in a given month/year (used by the
// Payroll Agent for salary deductions)")
////  public double getUnpaidLeaveDaysForMonth(
////      @ToolParam(description = "Employee ID") Long employeeId,
////      @ToolParam(description = "Month (1-12)") int month,
////      @ToolParam(description = "Year") int year) {
////    return leaveRequestRepository
////        .findByEmployeeIdAndStatus(employeeId, LeaveRequest.LeaveStatus.APPROVED)
////        .stream()
////        .filter(r -> r.getLeaveType() == LeaveRequest.LeaveType.UNPAID)
////        .filter(r -> overlapsMonth(r, month, year))
////        .mapToDouble(r -> ChronoUnit.DAYS.between(r.getStartDate(), r.getEndDate()) + 1)
////        .sum();
////  }
////
////  private boolean overlapsMonth(LeaveRequest r, int month, int year) {
////    LocalDate monthStart = LocalDate.of(year, month, 1);
////    LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
////    return !r.getStartDate().isAfter(monthEnd) && !r.getEndDate().isBefore(monthStart);
////  }
////  @Tool(description = "Get an employee's leave balances for a year")
////  public List<LeaveBalance> getLeaveBalances(
////      @ToolParam(description = "Employee ID") Long employeeId,
////      @ToolParam(description = "Year or null for current year") Integer year) {
////    return getLeaveBalance(employeeId, year);
////  }
////
////  @Tool(description = "Cancel a pending or approved leave request")
////  public LeaveRequest cancelLeave(@ToolParam(description = "Leave request ID") Long
// leaveRequestId) {
////    LeaveRequest request = leaveRequestRepository.findById(leaveRequestId)
////        .orElseThrow(() -> new IllegalArgumentException("No leave request found with id " +
// leaveRequestId));
////    if (request.getStatus() == LeaveRequest.LeaveStatus.CANCELLED) return request;
////    if (request.getStatus() == LeaveRequest.LeaveStatus.REJECTED)
////      throw new IllegalStateException("Rejected leave cannot be cancelled");
////    request.setStatus(LeaveRequest.LeaveStatus.CANCELLED);
////    return leaveRequestRepository.save(request);
////  }
////
////  @Tool(description = "Modify a leave request before it is finally completed")
////  public LeaveRequest modifyLeaveRequest(
////      @ToolParam(description = "Leave request ID") Long leaveRequestId,
////      @ToolParam(description = "New start date or null") LocalDate startDate,
////      @ToolParam(description = "New end date or null") LocalDate endDate,
////      @ToolParam(description = "New reason or null") String reason) {
////    LeaveRequest request = leaveRequestRepository.findById(leaveRequestId)
////        .orElseThrow(() -> new IllegalArgumentException("No leave request found with id " +
// leaveRequestId));
////    if (request.getStatus() != LeaveRequest.LeaveStatus.PENDING)
////      throw new IllegalStateException("Only pending leave requests can be modified");
////    if (startDate != null) request.setStartDate(startDate);
////    if (endDate != null) request.setEndDate(endDate);
////    if (reason != null) request.setReason(reason);
////    validateDates(request.getStartDate(), request.getEndDate());
////    return leaveRequestRepository.save(request);
////  }
////
////  @Tool(description = "Get complete leave history for an employee")
////  public List<LeaveRequest> getLeaveHistory(@ToolParam(description = "Employee ID") Long
// employeeId) {
////    return leaveRequestRepository.findByEmployeeId(employeeId);
////  }
////
////  @Tool(description = "Check whether an employee is eligible for a leave type and duration")
////  public EligibilityResult checkLeaveEligibility(
////      @ToolParam(description = "Employee ID") Long employeeId,
////      @ToolParam(description = "Leave type") LeaveRequest.LeaveType leaveType,
////      @ToolParam(description = "Start date") LocalDate startDate,
////      @ToolParam(description = "End date") LocalDate endDate) {
////    double days = calculateLeaveDuration(employeeId, startDate, endDate);
////    if (days <= 0) return new EligibilityResult(false, "End date must not precede start date",
// days);
////    if (leaveType == LeaveRequest.LeaveType.UNPAID) return new EligibilityResult(true, "Unpaid
// leave is eligible subject to approval", days);
////    LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYear(
////        employeeId, leaveType, startDate.getYear()).orElse(null);
////    if (balance == null) return new EligibilityResult(false, "No leave balance configured for
// this leave type", days);
////    return new EligibilityResult(balance.getRemainingDays() >= days,
////        balance.getRemainingDays() >= days ? "Sufficient leave balance" : "Insufficient leave
// balance", days);
////  }
////
////  @Tool(description = "Check overlap with existing leave requests")
////  public List<LeaveRequest> checkLeaveOverlap(
////      @ToolParam(description = "Employee ID") Long employeeId,
////      @ToolParam(description = "Start date") LocalDate startDate,
////      @ToolParam(description = "End date") LocalDate endDate) {
////    validateDates(startDate, endDate);
////    return leaveRequestRepository.findByEmployeeId(employeeId).stream()
////        .filter(r -> r.getStatus() != LeaveRequest.LeaveStatus.CANCELLED
////            && r.getStatus() != LeaveRequest.LeaveStatus.REJECTED
////            && !startDate.isAfter(r.getEndDate()) && !endDate.isBefore(r.getStartDate()))
////        .toList();
////  }
////
////  @Tool(description = "Calculate inclusive leave duration in days")
////  public double calculateLeaveDuration(
////      @ToolParam(description = "Employee ID") Long employeeId,
////      @ToolParam(description = "Start date") LocalDate startDate,
////      @ToolParam(description = "End date") LocalDate endDate) {
////    getEmployeeExists(employeeId);
////    validateDates(startDate, endDate);
////    return ChronoUnit.DAYS.between(startDate, endDate) + 1;
////  }
////
////  @Tool(description = "Check whether leave conflicts with weekends")
////  public ConflictResult checkWeekendConflict(
////      @ToolParam(description = "Start date") LocalDate startDate,
////      @ToolParam(description = "End date") LocalDate endDate) {
////    validateDates(startDate, endDate);
////    long weekendDays = startDate.datesUntil(endDate.plusDays(1))
////        .filter(d -> d.getDayOfWeek().getValue() >= 6).count();
////    return new ConflictResult(weekendDays > 0, weekendDays, "Weekend dates detected");
////  }
////
////  @Tool(description = "Check holiday conflict. The current service has no holiday table, so
// caller-supplied holiday dates are evaluated.")
////  public ConflictResult checkHolidayConflict(
////      @ToolParam(description = "Start date") LocalDate startDate,
////      @ToolParam(description = "End date") LocalDate endDate,
////      @ToolParam(description = "Comma-separated holiday dates yyyy-MM-dd") String holidayDates)
// {
////    validateDates(startDate, endDate);
////    long count = java.util.Arrays.stream(holidayDates == null ? new String[0] :
// holidayDates.split(","))
////        .map(String::trim).filter(s -> !s.isBlank()).map(LocalDate::parse)
////        .filter(d -> !d.isBefore(startDate) && !d.isAfter(endDate)).count();
////    return new ConflictResult(count > 0, count, "Holiday dates detected");
////  }
////
////  @Tool(description = "Get an employee leave calendar")
////  public List<LeaveRequest> getLeaveCalendar(
////      @ToolParam(description = "Employee ID") Long employeeId,
////      @ToolParam(description = "Start date") LocalDate startDate,
////      @ToolParam(description = "End date") LocalDate endDate) {
////    return getLeaveRequests(employeeId, null).stream()
////        .filter(r -> !r.getStartDate().isAfter(endDate) &&
// !r.getEndDate().isBefore(startDate)).toList();
////  }
////
////  @Tool(description = "Get leave calendar for a manager's team")
////  public List<LeaveRequest> getTeamLeaveCalendar(
////      @ToolParam(description = "Manager employee ID") Long managerEmployeeId,
////      @ToolParam(description = "Start date") LocalDate startDate,
////      @ToolParam(description = "End date") LocalDate endDate) {
////    List<Long> teamIds = employeeRepository.findByManagerEmployeeId(managerEmployeeId).stream()
////        .map(Employee::getEmployeeId).toList();
////    return leaveRequestRepository.findAll().stream()
////        .filter(r -> teamIds.contains(r.getEmployeeId()))
////        .filter(r -> !r.getStartDate().isAfter(endDate) && !r.getEndDate().isBefore(startDate))
////        .toList();
////  }
////
////  private void validateDates(LocalDate start, LocalDate end) {
////    if (start == null || end == null || end.isBefore(start))
////      throw new IllegalArgumentException("Invalid leave date range");
////  }
////
////  private void getEmployeeExists(Long employeeId) {
////    if (employeeId == null) throw new IllegalArgumentException("Employee ID is required");
////  }
////
////  public record EligibilityResult(boolean eligible, String reason, double requestedDays) {}
////  public record ConflictResult(boolean conflict, long matchingDays, String message) {}
////
//// }
// package com.hrms.mcpserver.tools;
//
// import com.hrms.mcpserver.domain.Employee;
// import com.hrms.mcpserver.domain.LeaveBalance;
// import com.hrms.mcpserver.domain.LeaveRequest;
// import com.hrms.mcpserver.repository.EmployeeRepository;
// import com.hrms.mcpserver.repository.LeaveBalanceRepository;
// import com.hrms.mcpserver.repository.LeaveRequestRepository;
// import java.time.LocalDate;
// import java.time.Year;
// import java.time.temporal.ChronoUnit;
// import java.util.Arrays;
// import java.util.List;
// import org.springframework.ai.tool.annotation.Tool;
// import org.springframework.ai.tool.annotation.ToolParam;
// import org.springframework.stereotype.Component;
// import org.springframework.transaction.annotation.Transactional;
//
/// **
// * Tools backing the Leave Agent — "Leave requests and balances". Called by the Manager Agent
// * (approvals), HR Agent (lifecycle checks) and Payroll Agent (unpaid-leave deductions), per the
// * responsibility table.
// */
// @Component
// public class LeaveTools {
//
//  private final LeaveRequestRepository leaveRequestRepository;
//  private final LeaveBalanceRepository leaveBalanceRepository;
//  private final EmployeeRepository employeeRepository;
//
//  public LeaveTools(
//          LeaveRequestRepository leaveRequestRepository,
//          LeaveBalanceRepository leaveBalanceRepository,
//          EmployeeRepository employeeRepository) {
//    this.leaveRequestRepository = leaveRequestRepository;
//    this.leaveBalanceRepository = leaveBalanceRepository;
//    this.employeeRepository = employeeRepository;
//  }
//
//  // ─────────────────────────────────────────────────────────────
//  // 1. APPLY FOR LEAVE
//  // ─────────────────────────────────────────────────────────────
//  @Tool(description = "Submit a new leave request for an employee")
//  @Transactional
//  public LeaveRequest applyForLeave(
//          @ToolParam(description = "Employee ID") Long employeeId,
//          @ToolParam(description = "Leave type: SICK, CASUAL, EARNED, UNPAID, MATERNITY,
// PATERNITY")
//          LeaveRequest.LeaveType leaveType,
//          @ToolParam(description = "Start date (yyyy-MM-dd)") LocalDate startDate,
//          @ToolParam(description = "End date (yyyy-MM-dd)") LocalDate endDate,
//          @ToolParam(description = "Reason for leave") String reason) {
//
//    validateEmployeeId(employeeId);
//    validateDates(startDate, endDate);
//
//    LeaveRequest request =
//            LeaveRequest.builder()
//                    .employeeId(employeeId)
//                    .leaveType(leaveType)
//                    .startDate(startDate)
//                    .endDate(endDate)
//                    .status(LeaveRequest.LeaveStatus.PENDING)
//                    .reason(reason)
//                    .build();
//    return leaveRequestRepository.save(request);
//  }
//
//  // ─────────────────────────────────────────────────────────────
//  // 2. APPROVE / REJECT
//  // ─────────────────────────────────────────────────────────────
//  @Tool(description = "Approve or reject a pending leave request (used by the Manager Agent)")
//  @Transactional
//  public LeaveRequest decideOnLeaveRequest(
//          @ToolParam(description = "Leave request ID") Long leaveRequestId,
//          @ToolParam(description = "true to approve, false to reject") boolean approve,
//          @ToolParam(description = "Manager employee ID making the decision")
//          String managerEmployeeId) {
//
//    LeaveRequest request =
//            leaveRequestRepository
//                    .findById(leaveRequestId)
//                    .orElseThrow(
//                            () ->
//                                    new IllegalArgumentException(
//                                            "No leave request found with id " + leaveRequestId));
//
//    if (request.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
//      throw new IllegalStateException(
//              "Only PENDING requests can be decided. Current status: " + request.getStatus());
//    }
//
//    request.setStatus(
//            approve ? LeaveRequest.LeaveStatus.APPROVED : LeaveRequest.LeaveStatus.REJECTED);
//    request.setApprovedByManagerId(managerEmployeeId);
//    LeaveRequest saved = leaveRequestRepository.save(request);
//
//    if (approve) {
//      applyLeaveToBalance(request);
//    }
//    return saved;
//  }
//
//  /**
//   * Applies approved leave to the employee's balance row.
//   *
//   * <p>UNPAID leave is skipped — payroll calculates it separately via {@link
//   * #getUnpaidLeaveDaysForMonth}.
//   *
//   * <p>If no balance row exists for the (employee, type, year), a row is created with zero quota
// —
//   * this preserves the original behaviour but is flagged in logs; HR should seed balances when an
//   * employee joins / a new year starts.
//   */
//  private void applyLeaveToBalance(LeaveRequest request) {
//    if (request.getLeaveType() == LeaveRequest.LeaveType.UNPAID) {
//      return; // payroll handles unpaid leave separately
//    }
//
//    int year = request.getStartDate().getYear();
//    double days = inclusiveDays(request.getStartDate(), request.getEndDate());
//
//    LeaveBalance balance =
//            leaveBalanceRepository
//                    .findByEmployeeIdAndLeaveTypeAndYear(
//                            request.getEmployeeId(), request.getLeaveType(), year)
//                    .orElseGet(
//                            () ->
//                                    LeaveBalance.builder()
//                                            .employeeId(request.getEmployeeId())
//                                            .leaveType(request.getLeaveType())
//                                            .year(year)
//                                            .totalDays(0)
//                                            .usedDays(0)
//                                            .remainingDays(0)
//                                            .build());
//
//    balance.setUsedDays(balance.getUsedDays() + days);
//    balance.setRemainingDays(balance.getTotalDays() - balance.getUsedDays());
//    leaveBalanceRepository.save(balance);
//  }
//
//  /** Reverts approved leave from the balance when a request is cancelled. */
//  private void revertLeaveFromBalance(LeaveRequest request) {
//    if (request.getLeaveType() == LeaveRequest.LeaveType.UNPAID) {
//      return;
//    }
//
//    int year = request.getStartDate().getYear();
//    double days = inclusiveDays(request.getStartDate(), request.getEndDate());
//
//    leaveBalanceRepository
//            .findByEmployeeIdAndLeaveTypeAndYear(
//                    request.getEmployeeId(), request.getLeaveType(), year)
//            .ifPresent(
//                    b -> {
//                      b.setUsedDays(Math.max(0, b.getUsedDays() - days));
//                      b.setRemainingDays(b.getTotalDays() - b.getUsedDays());
//                      leaveBalanceRepository.save(b);
//                    });
//  }
//
//  // ─────────────────────────────────────────────────────────────
//  // 3. READ REQUESTS
//  // ─────────────────────────────────────────────────────────────
//  @Tool(description = "Get all leave requests for an employee, optionally filtered by status")
//  public List<LeaveRequest> getLeaveRequests(
//          @ToolParam(description = "Employee ID") Long employeeId,
//          @ToolParam(
//                  description =
//                          "Status filter: PENDING, APPROVED, REJECTED, CANCELLED, or null for
// all")
//          LeaveRequest.LeaveStatus status) {
//    validateEmployeeId(employeeId);
//    if (status == null) return leaveRequestRepository.findByEmployeeId(employeeId);
//    return leaveRequestRepository.findByEmployeeIdAndStatus(employeeId, status);
//  }
//
//  // ─────────────────────────────────────────────────────────────
//  // 4. BALANCES
//  // ─────────────────────────────────────────────────────────────
//  @Tool(
//          description = "Get an employee's leave balances for a given year (defaults to current
// year)")
//  public List<LeaveBalance> getLeaveBalance(
//          @ToolParam(description = "Employee ID") Long employeeId,
//          @ToolParam(description = "Year, or null for current year") Integer year) {
//    validateEmployeeId(employeeId);
//    int y = (year != null) ? year : Year.now().getValue();
//    return leaveBalanceRepository.findByEmployeeIdAndYear(employeeId, y);
//  }
//
//  @Tool(description = "Get an employee's leave balances for a year")
//  public List<LeaveBalance> getLeaveBalances(
//          @ToolParam(description = "Employee ID") Long employeeId,
//          @ToolParam(description = "Year or null for current year") Integer year) {
//    return getLeaveBalance(employeeId, year);
//  }
//
//  // ─────────────────────────────────────────────────────────────
//  // 5. PAYROLL — UNPAID LEAVE
//  // ─────────────────────────────────────────────────────────────
//  @Tool(
//          description =
//                  "Get total unpaid-leave days taken by an employee in a given month/year (used by
// the Payroll Agent for salary deductions)")
//  public double getUnpaidLeaveDaysForMonth(
//          @ToolParam(description = "Employee ID") Long employeeId,
//          @ToolParam(description = "Month (1-12)") int month,
//          @ToolParam(description = "Year") int year) {
//    validateEmployeeId(employeeId);
//    return leaveRequestRepository
//            .findByEmployeeIdAndStatus(employeeId, LeaveRequest.LeaveStatus.APPROVED)
//            .stream()
//            .filter(r -> r.getLeaveType() == LeaveRequest.LeaveType.UNPAID)
//            .filter(r -> overlapsMonth(r, month, year))
//            .mapToDouble(r -> inclusiveDays(r.getStartDate(), r.getEndDate()))
//            .sum();
//  }
//
//  // ─────────────────────────────────────────────────────────────
//  // 6. CANCEL
//  // ─────────────────────────────────────────────────────────────
//  @Tool(description = "Cancel a pending or approved leave request")
//  @Transactional
//  public LeaveRequest cancelLeave(
//          @ToolParam(description = "Leave request ID") Long leaveRequestId) {
//    LeaveRequest request =
//            leaveRequestRepository
//                    .findById(leaveRequestId)
//                    .orElseThrow(
//                            () ->
//                                    new IllegalArgumentException(
//                                            "No leave request found with id " + leaveRequestId));
//
//    if (request.getStatus() == LeaveRequest.LeaveStatus.CANCELLED) return request;
//    if (request.getStatus() == LeaveRequest.LeaveStatus.REJECTED) {
//      throw new IllegalStateException("Rejected leave cannot be cancelled");
//    }
//
//    // If it was already approved, revert the balance
//    if (request.getStatus() == LeaveRequest.LeaveStatus.APPROVED) {
//      revertLeaveFromBalance(request);
//    }
//
//    request.setStatus(LeaveRequest.LeaveStatus.CANCELLED);
//    return leaveRequestRepository.save(request);
//  }
//
//  // ─────────────────────────────────────────────────────────────
//  // 7. MODIFY
//  // ─────────────────────────────────────────────────────────────
//  @Tool(description = "Modify a leave request before it is finally completed")
//  public LeaveRequest modifyLeaveRequest(
//          @ToolParam(description = "Leave request ID") Long leaveRequestId,
//          @ToolParam(description = "New start date or null") LocalDate startDate,
//          @ToolParam(description = "New end date or null") LocalDate endDate,
//          @ToolParam(description = "New reason or null") String reason) {
//
//    LeaveRequest request =
//            leaveRequestRepository
//                    .findById(leaveRequestId)
//                    .orElseThrow(
//                            () ->
//                                    new IllegalArgumentException(
//                                            "No leave request found with id " + leaveRequestId));
//
//    if (request.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
//      throw new IllegalStateException("Only pending leave requests can be modified");
//    }
//
//    if (startDate != null) request.setStartDate(startDate);
//    if (endDate != null) request.setEndDate(endDate);
//    if (reason != null) request.setReason(reason);
//
//    validateDates(request.getStartDate(), request.getEndDate());
//    return leaveRequestRepository.save(request);
//  }
//
//  // ─────────────────────────────────────────────────────────────
//  // 8. HISTORY
//  // ─────────────────────────────────────────────────────────────
//  @Tool(description = "Get complete leave history for an employee")
//  public List<LeaveRequest> getLeaveHistory(
//          @ToolParam(description = "Employee ID") Long employeeId) {
//    validateEmployeeId(employeeId);
//    return leaveRequestRepository.findByEmployeeId(employeeId);
//  }
//
//  // ─────────────────────────────────────────────────────────────
//  // 9. ELIGIBILITY
//  // ─────────────────────────────────────────────────────────────
//  @Tool(description = "Check whether an employee is eligible for a leave type and duration")
//  public EligibilityResult checkLeaveEligibility(
//          @ToolParam(description = "Employee ID") Long employeeId,
//          @ToolParam(description = "Leave type") LeaveRequest.LeaveType leaveType,
//          @ToolParam(description = "Start date") LocalDate startDate,
//          @ToolParam(description = "End date") LocalDate endDate) {
//
//    double days = calculateLeaveDuration(employeeId, startDate, endDate);
//    if (days <= 0) {
//      return new EligibilityResult(false, "End date must not precede start date", days);
//    }
//
//    if (leaveType == LeaveRequest.LeaveType.UNPAID) {
//      return new EligibilityResult(
//              true, "Unpaid leave is eligible subject to approval", days);
//    }
//
//    LeaveBalance balance =
//            leaveBalanceRepository
//                    .findByEmployeeIdAndLeaveTypeAndYear(employeeId, leaveType,
// startDate.getYear())
//                    .orElse(null);
//
//    if (balance == null) {
//      return new EligibilityResult(
//              false, "No leave balance configured for this leave type", days);
//    }
//
//    boolean enough = balance.getRemainingDays() >= days;
//    return new EligibilityResult(
//            enough, enough ? "Sufficient leave balance" : "Insufficient leave balance", days);
//  }
//
//  // ─────────────────────────────────────────────────────────────
//  // 10. OVERLAP
//  // ─────────────────────────────────────────────────────────────
//  @Tool(description = "Check overlap with existing leave requests")
//  public List<LeaveRequest> checkLeaveOverlap(
//          @ToolParam(description = "Employee ID") Long employeeId,
//          @ToolParam(description = "Start date") LocalDate startDate,
//          @ToolParam(description = "End date") LocalDate endDate) {
//
//    validateEmployeeId(employeeId);
//    validateDates(startDate, endDate);
//
//    return leaveRequestRepository.findByEmployeeId(employeeId).stream()
//            .filter(
//                    r ->
//                            r.getStatus() != LeaveRequest.LeaveStatus.CANCELLED
//                                    && r.getStatus() != LeaveRequest.LeaveStatus.REJECTED
//                                    && !startDate.isAfter(r.getEndDate())
//                                    && !endDate.isBefore(r.getStartDate()))
//            .toList();
//  }
//
//  // ─────────────────────────────────────────────────────────────
//  // 11. DURATION
//  // ─────────────────────────────────────────────────────────────
//  @Tool(description = "Calculate inclusive leave duration in days")
//  public double calculateLeaveDuration(
//          @ToolParam(description = "Employee ID") Long employeeId,
//          @ToolParam(description = "Start date") LocalDate startDate,
//          @ToolParam(description = "End date") LocalDate endDate) {
//
//    validateEmployeeId(employeeId);
//    validateDates(startDate, endDate);
//    return inclusiveDays(startDate, endDate);
//  }
//
//  // ─────────────────────────────────────────────────────────────
//  // 12. WEEKEND CONFLICT
//  // ─────────────────────────────────────────────────────────────
//  @Tool(description = "Check whether leave conflicts with weekends")
//  public ConflictResult checkWeekendConflict(
//          @ToolParam(description = "Start date") LocalDate startDate,
//          @ToolParam(description = "End date") LocalDate endDate) {
//
//    validateDates(startDate, endDate);
//    long weekendDays =
//            startDate
//                    .datesUntil(endDate.plusDays(1))
//                    .filter(d -> d.getDayOfWeek().getValue() >= 6)
//                    .count();
//    return new ConflictResult(weekendDays > 0, weekendDays, "Weekend dates detected");
//  }
//
//  // ─────────────────────────────────────────────────────────────
//  // 13. HOLIDAY CONFLICT
//  // ─────────────────────────────────────────────────────────────
//  @Tool(
//          description =
//                  "Check holiday conflict. The current service has no holiday table, so
// caller-supplied holiday dates are evaluated.")
//  public ConflictResult checkHolidayConflict(
//          @ToolParam(description = "Start date") LocalDate startDate,
//          @ToolParam(description = "End date") LocalDate endDate,
//          @ToolParam(description = "Comma-separated holiday dates yyyy-MM-dd") String
// holidayDates) {
//
//    validateDates(startDate, endDate);
//    long count =
//            Arrays.stream(holidayDates == null ? new String[0] : holidayDates.split(","))
//                    .map(String::trim)
//                    .filter(s -> !s.isBlank())
//                    .map(LocalDate::parse)
//                    .filter(d -> !d.isBefore(startDate) && !d.isAfter(endDate))
//                    .count();
//    return new ConflictResult(count > 0, count, "Holiday dates detected");
//  }
//
//  // ─────────────────────────────────────────────────────────────
//  // 14. CALENDAR
//  // ─────────────────────────────────────────────────────────────
//  @Tool(description = "Get an employee leave calendar")
//  public List<LeaveRequest> getLeaveCalendar(
//          @ToolParam(description = "Employee ID") Long employeeId,
//          @ToolParam(description = "Start date") LocalDate startDate,
//          @ToolParam(description = "End date") LocalDate endDate) {
//
//    validateEmployeeId(employeeId);
//    validateDates(startDate, endDate);
//
//    return getLeaveRequests(employeeId, null).stream()
//            .filter(
//                    r -> !r.getStartDate().isAfter(endDate) &&
// !r.getEndDate().isBefore(startDate))
//            .toList();
//  }
//
//  // ─────────────────────────────────────────────────────────────
//  // 15. TEAM CALENDAR
//  // ─────────────────────────────────────────────────────────────
//  @Tool(description = "Get leave calendar for a manager's team")
//  public List<LeaveRequest> getTeamLeaveCalendar(
//          @ToolParam(description = "Manager employee ID") Long managerEmployeeId,
//          @ToolParam(description = "Start date") LocalDate startDate,
//          @ToolParam(description = "End date") LocalDate endDate) {
//
//    validateEmployeeId(managerEmployeeId);
//    validateDates(startDate, endDate);
//
//    List<Long> teamIds =
//            employeeRepository.findByManagerEmployeeId(managerEmployeeId).stream()
//                    .map(Employee::getEmployeeId)
//                    .toList();
//
//    return leaveRequestRepository.findAll().stream()
//            .filter(r -> teamIds.contains(r.getEmployeeId()))
//            .filter(
//                    r -> !r.getStartDate().isAfter(endDate) &&
// !r.getEndDate().isBefore(startDate))
//            .toList();
//  }
//
//  // ─────────────────────────────────────────────────────────────
//  // HELPERS
//  // ─────────────────────────────────────────────────────────────
//  private double inclusiveDays(LocalDate start, LocalDate end) {
//    return ChronoUnit.DAYS.between(start, end) + 1;
//  }
//
//  private boolean overlapsMonth(LeaveRequest r, int month, int year) {
//    LocalDate monthStart = LocalDate.of(year, month, 1);
//    LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
//    return !r.getStartDate().isAfter(monthEnd) && !r.getEndDate().isBefore(monthStart);
//  }
//
//  private void validateDates(LocalDate start, LocalDate end) {
//    if (start == null || end == null || end.isBefore(start)) {
//      throw new IllegalArgumentException("Invalid leave date range");
//    }
//  }
//
//  private void validateEmployeeId(Long employeeId) {
//    if (employeeId == null) {
//      throw new IllegalArgumentException("Employee ID is required");
//    }
//  }
//  @Tool(description = "Initialize default leave balances for an employee for a given year. " +
//          "If employeeId is null, seeds for ALL employees.")
//  @Transactional
//  public List<LeaveBalance> seedLeaveBalances(
//          @ToolParam(description = "Employee ID, or null to seed for ALL employees")
//          Long employeeId,
//          @ToolParam(description = "Year or null for current year") Integer year) {
//
//    int y = (year != null) ? year : Year.now().getValue();
//    List<Long> employeeIds;
//
//    if (employeeId != null) {
//      employeeIds = List.of(employeeId);
//    } else {
//      // सगळ्या employees साठी
//      employeeIds = employeeRepository.findAll().stream()
//              .map(Employee::getEmployeeId)
//              .toList();
//    }
//
//    List<LeaveBalance> created = new java.util.ArrayList<>();
//
//    for (Long empId : employeeIds) {
//      for (LeaveRequest.LeaveType type : LeaveRequest.LeaveType.values()) {
//        if (type == LeaveRequest.LeaveType.UNPAID) continue;
//
//        boolean exists = leaveBalanceRepository
//                .findByEmployeeIdAndLeaveTypeAndYear(empId, type, y)
//                .isPresent();
//        if (exists) continue;
//
//        double quota = defaultQuotaFor(type);
//
//        created.add(leaveBalanceRepository.save(
//                LeaveBalance.builder()
//                        .employeeId(empId)
//                        .leaveType(type)
//                        .year(y)
//                        .totalDays(quota)
//                        .usedDays(0)
//                        .remainingDays(quota)
//                        .build()));
//      }
//    }
//    return created;
//  }
//
//  private double defaultQuotaFor(LeaveRequest.LeaveType type) {
//    return switch (type) {
//      case CASUAL    -> 12;
//      case SICK      -> 10;
//      case EARNED    -> 20;
//      case MATERNITY -> 182;
//      case PATERNITY -> 15;
//      default        -> 0;
//    };
//  }
//  // ─────────────────────────────────────────────────────────────
//  // RESULT RECORDS
//  // ─────────────────────────────────────────────────────────────
//  public record EligibilityResult(boolean eligible, String reason, double requestedDays) {}
//
//  public record ConflictResult(boolean conflict, long matchingDays, String message) {}
// }

package com.hrms.mcpserver.tools;

import com.hrms.mcpserver.domain.Employee;
import com.hrms.mcpserver.domain.Holiday;
import com.hrms.mcpserver.domain.LeaveBalance;
import com.hrms.mcpserver.domain.LeaveRequest;
import com.hrms.mcpserver.repository.EmployeeRepository;
import com.hrms.mcpserver.repository.HolidayRepository;
import com.hrms.mcpserver.repository.LeaveBalanceRepository;
import com.hrms.mcpserver.repository.LeaveRequestRepository;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
  private final HolidayRepository holidayRepository;

  public LeaveTools(
      LeaveRequestRepository leaveRequestRepository,
      LeaveBalanceRepository leaveBalanceRepository,
      EmployeeRepository employeeRepository,
      HolidayRepository holidayRepository) {
    this.leaveRequestRepository = leaveRequestRepository;
    this.leaveBalanceRepository = leaveBalanceRepository;
    this.employeeRepository = employeeRepository;
    this.holidayRepository = holidayRepository;
  }

  // ─────────────────────────────────────────────────────────────
  // 1. APPLY FOR LEAVE
  // ─────────────────────────────────────────────────────────────
  @Tool(description = "Submit a new leave request for an employee")
  @Transactional
  public LeaveRequest applyForLeave(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Leave type: SICK, CASUAL, EARNED, UNPAID")
          LeaveRequest.LeaveType leaveType,
      @ToolParam(description = "Start date (yyyy-MM-dd)") LocalDate startDate,
      @ToolParam(description = "End date (yyyy-MM-dd)") LocalDate endDate,
      @ToolParam(description = "Reason for leave") String reason) {

    validateEmployeeId(employeeId);
    validateDates(startDate, endDate);

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

  // ─────────────────────────────────────────────────────────────
  // 2. APPROVE / REJECT
  // ─────────────────────────────────────────────────────────────
  @Tool(description = "Approve or reject a pending leave request (used by the Manager Agent)")
  @Transactional
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

    if (request.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
      throw new IllegalStateException(
          "Only PENDING requests can be decided. Current status: " + request.getStatus());
    }

    request.setStatus(
        approve ? LeaveRequest.LeaveStatus.APPROVED : LeaveRequest.LeaveStatus.REJECTED);
    request.setApprovedByManagerId(managerEmployeeId);
    LeaveRequest saved = leaveRequestRepository.save(request);

    if (approve) {
      applyLeaveToBalance(request);
    }
    return saved;
  }

  /**
   * Applies approved leave to the employee's balance row. UNPAID leave is skipped — payroll
   * calculates it separately via {@link #getUnpaidLeaveDaysForMonth}.
   */
  private void applyLeaveToBalance(LeaveRequest request) {
    if (request.getLeaveType() == LeaveRequest.LeaveType.UNPAID) {
      return;
    }

    int year = request.getStartDate().getYear();
    double days = inclusiveDays(request.getStartDate(), request.getEndDate());

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

  /** Reverts approved leave from the balance when a request is cancelled. */
  private void revertLeaveFromBalance(LeaveRequest request) {
    if (request.getLeaveType() == LeaveRequest.LeaveType.UNPAID) {
      return;
    }

    int year = request.getStartDate().getYear();
    double days = inclusiveDays(request.getStartDate(), request.getEndDate());

    leaveBalanceRepository
        .findByEmployeeIdAndLeaveTypeAndYear(request.getEmployeeId(), request.getLeaveType(), year)
        .ifPresent(
            b -> {
              b.setUsedDays(Math.max(0, b.getUsedDays() - days));
              b.setRemainingDays(b.getTotalDays() - b.getUsedDays());
              leaveBalanceRepository.save(b);
            });
  }

  // ─────────────────────────────────────────────────────────────
  // 3. READ REQUESTS
  // ─────────────────────────────────────────────────────────────
  @Tool(description = "Get all leave requests for an employee, optionally filtered by status")
  public List<LeaveRequest> getLeaveRequests(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(
              description =
                  "Status filter: PENDING, APPROVED, REJECTED, CANCELLED, or null for all",
              required = false)
          LeaveRequest.LeaveStatus status) {
    validateEmployeeId(employeeId);
    if (status == null) return leaveRequestRepository.findByEmployeeId(employeeId);
    return leaveRequestRepository.findByEmployeeIdAndStatus(employeeId, status);
  }

  // ─────────────────────────────────────────────────────────────
  // 4. BALANCES
  // ─────────────────────────────────────────────────────────────
  @Tool(
      description = "Get an employee's leave balances for a given year (defaults to current year)")
  public List<LeaveBalance> getLeaveBalance(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Year in yyyy format (defaults to current year)", required = false)
          Integer year) {
    validateEmployeeId(employeeId);
    int y = (year != null) ? year : Year.now().getValue();
    return leaveBalanceRepository.findByEmployeeIdAndYear(employeeId, y);
  }

  // ─────────────────────────────────────────────────────────────
  // 5. PAYROLL — UNPAID LEAVE
  // ─────────────────────────────────────────────────────────────
  @Tool(
      description =
          "Get total unpaid-leave days taken by an employee in a given month/year (used by the Payroll Agent for salary deductions)")
  public double getUnpaidLeaveDaysForMonth(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Month (1-12)") int month,
      @ToolParam(description = "Year") int year) {
    validateEmployeeId(employeeId);
    return leaveRequestRepository
        .findByEmployeeIdAndStatus(employeeId, LeaveRequest.LeaveStatus.APPROVED)
        .stream()
        .filter(r -> r.getLeaveType() == LeaveRequest.LeaveType.UNPAID)
        .filter(r -> overlapsMonth(r, month, year))
        .mapToDouble(r -> inclusiveDays(r.getStartDate(), r.getEndDate()))
        .sum();
  }

  // ─────────────────────────────────────────────────────────────
  // 6. CANCEL
  // ─────────────────────────────────────────────────────────────
  @Tool(description = "Cancel a pending or approved leave request")
  @Transactional
  public LeaveRequest cancelLeave(
      @ToolParam(description = "Leave request ID") Long leaveRequestId) {
    LeaveRequest request =
        leaveRequestRepository
            .findById(leaveRequestId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "No leave request found with id " + leaveRequestId));

    if (request.getStatus() == LeaveRequest.LeaveStatus.CANCELLED) return request;
    if (request.getStatus() == LeaveRequest.LeaveStatus.REJECTED) {
      throw new IllegalStateException("Rejected leave cannot be cancelled");
    }

    if (request.getStatus() == LeaveRequest.LeaveStatus.APPROVED) {
      revertLeaveFromBalance(request);
    }

    request.setStatus(LeaveRequest.LeaveStatus.CANCELLED);
    return leaveRequestRepository.save(request);
  }

  // ─────────────────────────────────────────────────────────────
  // 7. MODIFY
  // ─────────────────────────────────────────────────────────────
  @Tool(description = "Modify a leave request before it is finally completed")
  public LeaveRequest modifyLeaveRequest(
      @ToolParam(description = "Leave request ID") Long leaveRequestId,
      @ToolParam(description = "New start date or null", required = false) LocalDate startDate,
      @ToolParam(description = "New end date or null", required = false) LocalDate endDate,
      @ToolParam(description = "New reason or null", required = false) String reason) {

    LeaveRequest request =
        leaveRequestRepository
            .findById(leaveRequestId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "No leave request found with id " + leaveRequestId));

    if (request.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
      throw new IllegalStateException("Only pending leave requests can be modified");
    }

    if (startDate != null) request.setStartDate(startDate);
    if (endDate != null) request.setEndDate(endDate);
    if (reason != null) request.setReason(reason);

    validateDates(request.getStartDate(), request.getEndDate());
    return leaveRequestRepository.save(request);
  }

  // ─────────────────────────────────────────────────────────────
  // 8. HISTORY
  // ─────────────────────────────────────────────────────────────
  @Tool(description = "Get complete leave history for an employee")
  public List<LeaveRequest> getLeaveHistory(
      @ToolParam(description = "Employee ID") Long employeeId) {
    validateEmployeeId(employeeId);
    return leaveRequestRepository.findByEmployeeId(employeeId);
  }

  // ─────────────────────────────────────────────────────────────
  // 9. ELIGIBILITY
  // ─────────────────────────────────────────────────────────────
  @Tool(description = "Check whether an employee is eligible for a leave type and duration")
  public EligibilityResult checkLeaveEligibility(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Leave type") LeaveRequest.LeaveType leaveType,
      @ToolParam(description = "Start date") LocalDate startDate,
      @ToolParam(description = "End date") LocalDate endDate) {

    double days = calculateLeaveDuration(employeeId, startDate, endDate);
    if (days <= 0) {
      return new EligibilityResult(false, "End date must not precede start date", days);
    }

    if (leaveType == LeaveRequest.LeaveType.UNPAID) {
      return new EligibilityResult(true, "Unpaid leave is eligible subject to approval", days);
    }

    LeaveBalance balance =
        leaveBalanceRepository
            .findByEmployeeIdAndLeaveTypeAndYear(employeeId, leaveType, startDate.getYear())
            .orElse(null);

    if (balance == null) {
      return new EligibilityResult(false, "No leave balance configured for this leave type", days);
    }

    boolean enough = balance.getRemainingDays() >= days;
    return new EligibilityResult(
        enough, enough ? "Sufficient leave balance" : "Insufficient leave balance", days);
  }

  // ─────────────────────────────────────────────────────────────
  // 10. OVERLAP
  // ─────────────────────────────────────────────────────────────
  @Tool(description = "Check overlap with existing leave requests")
  public List<LeaveRequest> checkLeaveOverlap(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Start date") LocalDate startDate,
      @ToolParam(description = "End date") LocalDate endDate) {

    validateEmployeeId(employeeId);
    validateDates(startDate, endDate);

    return leaveRequestRepository.findByEmployeeId(employeeId).stream()
        .filter(
            r ->
                r.getStatus() != LeaveRequest.LeaveStatus.CANCELLED
                    && r.getStatus() != LeaveRequest.LeaveStatus.REJECTED
                    && !startDate.isAfter(r.getEndDate())
                    && !endDate.isBefore(r.getStartDate()))
        .toList();
  }

  // ─────────────────────────────────────────────────────────────
  // 11. DURATION
  // ─────────────────────────────────────────────────────────────
  @Tool(description = "Calculate inclusive leave duration in days")
  public double calculateLeaveDuration(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Start date") LocalDate startDate,
      @ToolParam(description = "End date") LocalDate endDate) {

    validateEmployeeId(employeeId);
    validateDates(startDate, endDate);
    return inclusiveDays(startDate, endDate);
  }

  // ─────────────────────────────────────────────────────────────
  // 12. WEEKEND CONFLICT
  // ─────────────────────────────────────────────────────────────
  @Tool(description = "Check whether leave conflicts with weekends")
  public ConflictResult checkWeekendConflict(
      @ToolParam(description = "Start date") LocalDate startDate,
      @ToolParam(description = "End date") LocalDate endDate) {

    validateDates(startDate, endDate);
    long weekendDays =
        startDate
            .datesUntil(endDate.plusDays(1))
            .filter(d -> d.getDayOfWeek().getValue() >= 6)
            .count();

    return new ConflictResult(
        weekendDays > 0,
        weekendDays,
        weekendDays > 0 ? weekendDays + " weekend day(s) in range" : "No weekend conflict");
  }

  // ─────────────────────────────────────────────────────────────
  // 13. HOLIDAY CONFLICT — DB based
  // ─────────────────────────────────────────────────────────────
  @Tool(description = "Check if leave dates conflict with company holidays (fetched from DB)")
  public ConflictResult checkHolidayConflict(
      @ToolParam(description = "Start date") LocalDate startDate,
      @ToolParam(description = "End date") LocalDate endDate) {

    validateDates(startDate, endDate);

    List<Holiday> holidays = holidayRepository.findByDateBetweenOrderByDateAsc(startDate, endDate);
    long count = holidays.size();

    return new ConflictResult(
        count > 0,
        count,
        count > 0
            ? count + " holiday(s): " + holidays.stream().map(Holiday::getName).toList()
            : "No holiday conflict");
  }

  // ─────────────────────────────────────────────────────────────
  // 14. CALENDAR
  // ─────────────────────────────────────────────────────────────
  @Tool(description = "Get an employee leave calendar")
  public List<LeaveRequest> getLeaveCalendar(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Start date") LocalDate startDate,
      @ToolParam(description = "End date") LocalDate endDate) {

    validateEmployeeId(employeeId);
    validateDates(startDate, endDate);

    return getLeaveRequests(employeeId, null).stream()
        .filter(r -> !r.getStartDate().isAfter(endDate) && !r.getEndDate().isBefore(startDate))
        .toList();
  }

  // ─────────────────────────────────────────────────────────────
  // 15. TEAM CALENDAR
  // ─────────────────────────────────────────────────────────────
  @Tool(description = "Get leave calendar for a manager's team")
  public List<LeaveRequest> getTeamLeaveCalendar(
      @ToolParam(description = "Manager employee ID") Long managerEmployeeId,
      @ToolParam(description = "Start date") LocalDate startDate,
      @ToolParam(description = "End date") LocalDate endDate) {

    validateEmployeeId(managerEmployeeId);
    validateDates(startDate, endDate);

    List<Long> teamIds =
        employeeRepository.findByManagerEmployeeId(managerEmployeeId).stream()
            .map(Employee::getEmployeeId)
            .toList();

    return leaveRequestRepository.findAll().stream()
        .filter(r -> teamIds.contains(r.getEmployeeId()))
        .filter(r -> !r.getStartDate().isAfter(endDate) && !r.getEndDate().isBefore(startDate))
        .toList();
  }

  // ─────────────────────────────────────────────────────────────
  // 16. SEED BALANCES
  // ─────────────────────────────────────────────────────────────
  @Tool(
      description =
          "Initialize default leave balances for an employee for a given year. If employeeId is null, seeds for ALL employees.")
  @Transactional
  public List<LeaveBalance> seedLeaveBalances(
      @ToolParam(description = "Employee ID, or null to seed for ALL employees", required = false)
          Long employeeId,
      @ToolParam(description = "Year (defaults to current year)", required = false) Integer year) {

    int y = (year != null) ? year : Year.now().getValue();

    List<Long> employeeIds =
        (employeeId != null)
            ? List.of(employeeId)
            : employeeRepository.findAll().stream().map(Employee::getEmployeeId).toList();

    List<LeaveBalance> created = new ArrayList<>();

    for (Long empId : employeeIds) {
      for (LeaveRequest.LeaveType type : LeaveRequest.LeaveType.values()) {
        if (type == LeaveRequest.LeaveType.UNPAID) continue;

        boolean exists =
            leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYear(empId, type, y).isPresent();
        if (exists) continue;

        double quota = defaultQuotaFor(type);

        created.add(
            leaveBalanceRepository.save(
                LeaveBalance.builder()
                    .employeeId(empId)
                    .leaveType(type)
                    .year(y)
                    .totalDays(quota)
                    .usedDays(0)
                    .remainingDays(quota)
                    .build()));
      }
    }
    return created;
  }

  private double defaultQuotaFor(LeaveRequest.LeaveType type) {
    return switch (type) {
      case CASUAL -> 12;
      case SICK -> 10;
      case EARNED -> 20;

      default -> 0;
    };
  }

  // ─────────────────────────────────────────────────────────────
  // HELPERS
  // ─────────────────────────────────────────────────────────────
  private double inclusiveDays(LocalDate start, LocalDate end) {
    return ChronoUnit.DAYS.between(start, end) + 1;
  }

  private boolean overlapsMonth(LeaveRequest r, int month, int year) {
    LocalDate monthStart = LocalDate.of(year, month, 1);
    LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
    return !r.getStartDate().isAfter(monthEnd) && !r.getEndDate().isBefore(monthStart);
  }

  private void validateDates(LocalDate start, LocalDate end) {
    if (start == null || end == null || end.isBefore(start)) {
      throw new IllegalArgumentException("Invalid leave date range");
    }
  }

  private void validateEmployeeId(Long employeeId) {
    if (employeeId == null) {
      throw new IllegalArgumentException("Employee ID is required");
    }
  }

  // ─────────────────────────────────────────────────────────────
  // RESULT RECORDS
  // ─────────────────────────────────────────────────────────────
  public record EligibilityResult(boolean eligible, String reason, double requestedDays) {}

  public record ConflictResult(boolean conflict, long matchingDays, String message) {}
}
