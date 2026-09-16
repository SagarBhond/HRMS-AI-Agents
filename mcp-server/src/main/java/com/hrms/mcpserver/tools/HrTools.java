package com.hrms.mcpserver.tools;

import com.hrms.mcpserver.domain.Employee;
import com.hrms.mcpserver.domain.HR;
import com.hrms.mcpserver.repository.HRRepository;
import com.hrms.mcpserver.repository.EmployeeRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Tools backing the HR Agent — "Employee lifecycle". Per the responsibility table, HR calls Payroll
 * (final settlement / revised CTC) and Budget (headcount cost). Budget is a separate agent outside
 * this project's scope (Employee/HR/Leave/Payroll/Attendance only, per the architecture diagram),
 * so only the Payroll hook is wired in-process here.
 */
@Component
public class HrTools {

  private final HRRepository lifecycleEventRepository;
  private final EmployeeRepository employeeRepository;
  private final PayrollTools payrollTools;

  public HrTools(
      HRRepository lifecycleEventRepository,
      EmployeeRepository employeeRepository,
      PayrollTools payrollTools) {
    this.lifecycleEventRepository = lifecycleEventRepository;
    this.employeeRepository = employeeRepository;
    this.payrollTools = payrollTools;
  }

  @Tool(description = "Record an onboarding event for a new employee")
  public HR recordOnboarding(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Onboarding date (yyyy-MM-dd)") LocalDate eventDate,
      @ToolParam(description = "Onboarding notes") String details) {
    return saveEvent(
        employeeId, HR.LifecycleEventType.ONBOARDING, eventDate, details);
  }

  @Tool(
      description =
          "Record a promotion or designation change for an employee, and update their profile")
  public HR recordPromotion(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Effective date (yyyy-MM-dd)") LocalDate eventDate,
      @ToolParam(description = "New designation") String newDesignation,
      @ToolParam(description = "Promotion notes") String details) {
    Employee employee =
        employeeRepository
            .findById(employeeId)
            .orElseThrow(
                () -> new IllegalArgumentException("No employee found with id " + employeeId));
    employee.setDesignation(newDesignation);
    employeeRepository.save(employee);
    return saveEvent(
        employeeId, HR.LifecycleEventType.PROMOTION, eventDate, details);
  }

  @Tool(description = "Record a department transfer for an employee, and update their profile")
  public HR recordTransfer(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Effective date (yyyy-MM-dd)") LocalDate eventDate,
      @ToolParam(description = "New department") String newDepartment,
      @ToolParam(description = "Transfer notes") String details) {
    Employee employee =
        employeeRepository
            .findById(employeeId)
            .orElseThrow(
                () -> new IllegalArgumentException("No employee found with id " + employeeId));
    employee.setDepartment(newDepartment);
    employeeRepository.save(employee);
    return saveEvent(
        employeeId, HR.LifecycleEventType.TRANSFER, eventDate, details);
  }

  @Tool(
      description =
          "Record an employee exit: marks the employee TERMINATED, triggers final payroll settlement, and logs the lifecycle event")
  public HR recordExit(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Exit date (yyyy-MM-dd)") LocalDate eventDate,
      @ToolParam(description = "Exit reason / notes") String details,
      @ToolParam(description = "Final basic salary for settlement") double finalBasicSalary,
      @ToolParam(description = "Final allowances for settlement") double finalAllowances,
      @ToolParam(description = "Per-day rate for unpaid-leave deduction") double perDayRate) {
    Employee employee =
        employeeRepository
            .findById(employeeId)
            .orElseThrow(
                () -> new IllegalArgumentException("No employee found with id " + employeeId));
    employee.setStatus(Employee.EmployeeStatus.TERMINATED);
    employeeRepository.save(employee);

    // HR -> Payroll: trigger final settlement for the exit month.
    payrollTools.generateSalarySlip(
        employeeId,
        eventDate.getMonthValue(),
        eventDate.getYear(),
        finalBasicSalary,
        finalAllowances,
        perDayRate);

    return saveEvent(
        employeeId, HR.LifecycleEventType.EXIT, eventDate, details);
  }

  @Tool(description = "Get the full lifecycle event history for an employee")
  public List<HR> getLifecycleHistory(
      @ToolParam(description = "Employee ID") Long employeeId) {
    return lifecycleEventRepository.findByEmployeeId(employeeId);
  }

  private HR saveEvent(
      Long employeeId,
      HR.LifecycleEventType type,
      LocalDate eventDate,
      String details) {
    HR event =
        HR.builder()
            .employeeId(employeeId)
            .eventType(type)
            .eventDate(eventDate)
            .details(details)
            .build();
    return lifecycleEventRepository.save(event);
  }
}
