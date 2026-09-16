package com.hrms.mcpserver.tools;

import com.hrms.mcpserver.domain.Attendance;
import com.hrms.mcpserver.domain.Employee;
import com.hrms.mcpserver.repository.AttendanceRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.hrms.mcpserver.repository.EmployeeRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Tools backing the Attendance Agent — "Attendance and working hours". Consumed by the Payroll
 * Agent when generating salaries, per the responsibility table.
 */
@Component
public class AttendanceTools {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public AttendanceTools(
            AttendanceRepository attendanceRepository,
            EmployeeRepository employeeRepository) {

        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

  @Tool(description = "Record a check-in for an employee on a given work date")
  public Attendance checkIn(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Work date (yyyy-MM-dd)") LocalDate workDate,
      @ToolParam(description = "Check-in time (HH:mm)") LocalTime checkInTime) {
    Attendance attendance =
        Attendance.builder()
            .employeeId(employeeId)
            .workDate(workDate)
            .checkIn(checkInTime)
            .status(Attendance.AttendanceStatus.PRESENT)
            .build();
    return attendanceRepository.save(attendance);
  }

  @Tool(
      description =
          "Record a check-out for an employee for the given work date and compute hours worked")
  public Attendance checkOut(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Work date (yyyy-MM-dd)") LocalDate workDate,
      @ToolParam(description = "Check-out time (HH:mm)") LocalTime checkOutTime) {

    Attendance attendance =
        attendanceRepository
            .findByEmployeeIdAndWorkDate(employeeId, workDate)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "No attendance record found for employee "
                            + employeeId
                            + " on "
                            + workDate));

    if (attendance.getCheckIn() == null) {
      throw new IllegalStateException("Employee has no check-in record for " + workDate);
    }

    if (attendance.getCheckOut() != null) {
      throw new IllegalStateException("Employee has already checked out for " + workDate);
    }

    attendance.setCheckOut(checkOutTime);

    double hours = Duration.between(attendance.getCheckIn(), checkOutTime).toMinutes() / 60.0;

    attendance.setHoursWorked(Math.max(hours, 0));

    attendance.setStatus(
        hours < 4 ? Attendance.AttendanceStatus.HALF_DAY : Attendance.AttendanceStatus.PRESENT);

    return attendanceRepository.save(attendance);
  }

  @Tool(
      description =
          "Mark an employee absent, on leave, on a holiday, or on a week-off for a given date")
  public Attendance markStatus(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Work date (yyyy-MM-dd)") LocalDate workDate,
      @ToolParam(description = "Status: ABSENT, ON_LEAVE, HOLIDAY, WEEK_OFF")
          Attendance.AttendanceStatus status) {
    Attendance attendance =
        Attendance.builder()
            .employeeId(employeeId)
            .workDate(workDate)
            .status(status)
            .hoursWorked(0)
            .build();
    return attendanceRepository.save(attendance);
  }

  @Tool(description = "Get an employee's attendance records between two dates (inclusive)")
  public List<Attendance> getAttendanceForRange(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Start date (yyyy-MM-dd)") LocalDate start,
      @ToolParam(description = "End date (yyyy-MM-dd)") LocalDate end) {
    return attendanceRepository.findByEmployeeIdAndWorkDateBetween(employeeId, start, end);
  }

  @Tool(
      description =
          "Get total hours worked and present-days count for an employee in a given month (used by the Payroll Agent)")
  public MonthlyAttendanceSummary getMonthlyAttendanceSummary(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Month (1-12)") int month,
      @ToolParam(description = "Year") int year) {
    LocalDate start = LocalDate.of(year, month, 1);
    LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
    List<Attendance> records =
        attendanceRepository.findByEmployeeIdAndWorkDateBetween(employeeId, start, end);

    double totalHours = records.stream().mapToDouble(Attendance::getHoursWorked).sum();
    long presentDays =
        records.stream()
            .filter(
                a ->
                    a.getStatus() == Attendance.AttendanceStatus.PRESENT
                        || a.getStatus() == Attendance.AttendanceStatus.HALF_DAY)
            .count();
    long absentDays =
        records.stream().filter(a -> a.getStatus() == Attendance.AttendanceStatus.ABSENT).count();

    return new MonthlyAttendanceSummary(
        employeeId, month, year, totalHours, presentDays, absentDays);
  }

  public record MonthlyAttendanceSummary(
      Long employeeId,
      int month,
      int year,
      double totalHoursWorked,
      long presentDays,
      long absentDays) {}

    @Tool(
            description =
                    "Get attendance records for all employees reporting to a manager for a specific work date")
    public List<Attendance> getTeamAttendance(
            @ToolParam(description = "Manager employee ID") Long managerEmployeeId,
            @ToolParam(description = "Work date (yyyy-MM-dd)") LocalDate workDate) {

        List<Long> teamEmployeeIds =
                employeeRepository.findByManagerEmployeeId(managerEmployeeId)
                        .stream()
                        .map(Employee::getEmployeeId)
                        .toList();

        if (teamEmployeeIds.isEmpty()) {
            return List.of();
        }

        return attendanceRepository
                .findByEmployeeIdInAndWorkDate(teamEmployeeIds, workDate);
    }
  @Tool(
          description =
                  "Calculate an employee's working hours and overtime for a specific work date")
  public WorkingHoursResult getWorkingHours(
          @ToolParam(description = "Employee ID") Long employeeId,
          @ToolParam(description = "Work date (yyyy-MM-dd)") LocalDate workDate) {

    Attendance attendance =
            attendanceRepository
                    .findByEmployeeIdAndWorkDate(employeeId, workDate)
                    .orElseThrow(
                            () ->
                                    new IllegalArgumentException(
                                            "No attendance record found for employee "
                                                    + employeeId
                                                    + " on "
                                                    + workDate));

    LocalTime checkIn = attendance.getCheckIn();
    LocalTime checkOut = attendance.getCheckOut();

    if (checkIn == null || checkOut == null) {
      return new WorkingHoursResult(
              employeeId,
              workDate,
              checkIn,
              checkOut,
              0,
              0);
    }

    long workingMinutes =
            Duration.between(checkIn, checkOut).toMinutes();

    long standardWorkingMinutes = 8 * 60;

    long overtimeMinutes =
            Math.max(
                    workingMinutes - standardWorkingMinutes,
                    0);

    return new WorkingHoursResult(
            employeeId,
            workDate,
            checkIn,
            checkOut,
            workingMinutes,
            overtimeMinutes);
  }

  public record WorkingHoursResult(
          Long employeeId,
          LocalDate workDate,
          LocalTime checkIn,
          LocalTime checkOut,
          long workingMinutes,
          long overtimeMinutes) {}
  @Tool(description = "Get overtime minutes worked by an employee on a specific work date")
  public OvertimeResult getOvertime(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Work date (yyyy-MM-dd)") LocalDate workDate) {
    WorkingHoursResult hours = getWorkingHours(employeeId, workDate);
    return new OvertimeResult(employeeId, workDate, hours.overtimeMinutes(), hours.overtimeMinutes() / 60.0);
  }

  public record OvertimeResult(Long employeeId, LocalDate workDate, long overtimeMinutes, double overtimeHours) {}

  @Tool(description = "Get today's attendance for an employee")
  public Attendance getTodayAttendance(@ToolParam(description = "Employee ID") Long employeeId) {
    return attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, LocalDate.now()).orElse(null);
  }

  @Tool(description = "Get attendance history between dates")
  public List<Attendance> getAttendanceHistory(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Start date") LocalDate startDate,
      @ToolParam(description = "End date") LocalDate endDate) {
    return getAttendanceForRange(employeeId, startDate, endDate);
  }

  @Tool(description = "Get monthly attendance")
  public List<Attendance> getMonthlyAttendance(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Month 1-12") int month,
      @ToolParam(description = "Year") int year) {
    LocalDate start = LocalDate.of(year, month, 1);
    return getAttendanceForRange(employeeId, start, start.withDayOfMonth(start.lengthOfMonth()));
  }

  @Tool(description = "Get attendance summary for a month")
  public MonthlyAttendanceSummary getAttendanceSummary(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Month 1-12") int month,
      @ToolParam(description = "Year") int year) {
    return getMonthlyAttendanceSummary(employeeId, month, year);
  }

  @Tool(description = "Calculate attendance percentage for a month")
  public AttendancePercentage calculateAttendancePercentage(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Month 1-12") int month,
      @ToolParam(description = "Year") int year) {
    List<Attendance> records = getMonthlyAttendance(employeeId, month, year);
    long working = records.stream().filter(a -> a.getStatus() != Attendance.AttendanceStatus.HOLIDAY
        && a.getStatus() != Attendance.AttendanceStatus.WEEK_OFF).count();
    long present = records.stream().filter(a -> a.getStatus() == Attendance.AttendanceStatus.PRESENT
        || a.getStatus() == Attendance.AttendanceStatus.HALF_DAY).count();
    return new AttendancePercentage(employeeId, month, year, present, working,
        working == 0 ? 0 : present * 100.0 / working);
  }

  @Tool(description = "Get late arrivals after a specified time")
  public List<Attendance> getLateArrivals(
      @ToolParam(description = "Employee ID, or null for all employees") Long employeeId,
      @ToolParam(description = "Start date") LocalDate startDate,
      @ToolParam(description = "End date") LocalDate endDate,
      @ToolParam(description = "Expected check-in time HH:mm") LocalTime expectedCheckIn) {
    List<Attendance> source = employeeId == null
        ? attendanceRepository.findAll().stream().filter(a -> !a.getWorkDate().isBefore(startDate) && !a.getWorkDate().isAfter(endDate)).toList()
        : getAttendanceForRange(employeeId, startDate, endDate);
    return source.stream().filter(a -> a.getCheckIn() != null && a.getCheckIn().isAfter(expectedCheckIn)).toList();
  }

  @Tool(description = "Get early departures before a specified time")
  public List<Attendance> getEarlyDepartures(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Start date") LocalDate startDate,
      @ToolParam(description = "End date") LocalDate endDate,
      @ToolParam(description = "Expected check-out time HH:mm") LocalTime expectedCheckOut) {
    return getAttendanceForRange(employeeId, startDate, endDate).stream()
        .filter(a -> a.getCheckOut() != null && a.getCheckOut().isBefore(expectedCheckOut)).toList();
  }

  @Tool(description = "Get absence summary for a month")
  public AbsenceSummary getAbsenceSummary(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Month 1-12") int month,
      @ToolParam(description = "Year") int year) {
    MonthlyAttendanceSummary s = getMonthlyAttendanceSummary(employeeId, month, year);
    return new AbsenceSummary(employeeId, month, year, s.absentDays());
  }

  @Tool(description = "Get attendance trend for recent months")
  public List<AttendancePercentage> getAttendanceTrend(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Number of months") int months,
      @ToolParam(description = "Ending year") int year,
      @ToolParam(description = "Ending month 1-12") int month) {
    List<AttendancePercentage> result = new java.util.ArrayList<>();
    java.time.YearMonth ym = java.time.YearMonth.of(year, month);
    for (int i = Math.max(months, 1) - 1; i >= 0; i--) {
      java.time.YearMonth current = ym.minusMonths(i);
      result.add(calculateAttendancePercentage(employeeId, current.getMonthValue(), current.getYear()));
    }
    return result;
  }

  @Tool(description = "Generate an attendance report")
  public AttendanceReport generateAttendanceReport(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Month 1-12") int month,
      @ToolParam(description = "Year") int year) {
    return new AttendanceReport(employeeId, month, year,
        getMonthlyAttendanceSummary(employeeId, month, year),
        calculateAttendancePercentage(employeeId, month, year));
  }

  @Tool(description = "Generate a team attendance report")
  public TeamAttendanceReport generateTeamAttendanceReport(
      @ToolParam(description = "Manager employee ID") Long managerEmployeeId,
      @ToolParam(description = "Work date") LocalDate workDate) {
    return new TeamAttendanceReport(managerEmployeeId, workDate, getTeamAttendance(managerEmployeeId, workDate));
  }

  @Tool(description = "Detect attendance issues for an employee")
  public List<String> detectAttendanceIssues(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Month 1-12") int month,
      @ToolParam(description = "Year") int year) {
    AttendancePercentage p = calculateAttendancePercentage(employeeId, month, year);
    List<String> issues = new java.util.ArrayList<>();
    if (p.percentage() < 90) issues.add("Attendance is below 90%");
    getMonthlyAttendance(employeeId, month, year).stream()
        .filter(a -> a.getCheckIn() != null && a.getCheckIn().isAfter(LocalTime.of(9, 30)))
        .forEach(a -> issues.add("Late arrival on " + a.getWorkDate()));
    return issues;
  }

  public record AttendancePercentage(Long employeeId, int month, int year, long presentDays, long workingDays, double percentage) {}
  public record AbsenceSummary(Long employeeId, int month, int year, long absentDays) {}
  public record AttendanceReport(Long employeeId, int month, int year, MonthlyAttendanceSummary summary, AttendancePercentage percentage) {}
  public record TeamAttendanceReport(Long managerEmployeeId, LocalDate workDate, List<Attendance> records) {}

}
