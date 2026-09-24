package com.hrms.mcpserver.tools;

import com.hrms.mcpserver.domain.Payroll;
import com.hrms.mcpserver.repository.PayrollRepository;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class PayrollTools {

  private final PayrollRepository salarySlipRepository;
  private final AttendanceTools attendanceTools;
  private final LeaveTools leaveTools;

  public PayrollTools(
      PayrollRepository salarySlipRepository,
      AttendanceTools attendanceTools,
      LeaveTools leaveTools) {
    this.salarySlipRepository = salarySlipRepository;
    this.attendanceTools = attendanceTools;
    this.leaveTools = leaveTools;
  }

  @Tool(description = "Generate salary slip")
  public Payroll generateSalarySlip(
      Long employeeId,
      int month,
      int year,
      double basicSalary,
      double allowances,
      double perDayRate) {
    double unpaidLeaveDeduction =
        leaveTools.getUnpaidLeaveDaysForMonth(employeeId, month, year) * perDayRate;
    double deductions = unpaidLeaveDeduction;
    double netSalary = basicSalary + allowances - deductions;
    Payroll slip =
        salarySlipRepository
            .findByEmployeeIdAndMonthAndYear(employeeId, month, year)
            .orElseGet(Payroll::new);
    slip.setEmployeeId(employeeId);
    slip.setMonth(month);
    slip.setYear(year);
    slip.setBasicSalary(basicSalary);
    slip.setAllowances(allowances);
    slip.setUnpaidLeaveDeduction(unpaidLeaveDeduction);
    slip.setDeductions(deductions);
    slip.setNetSalary(netSalary);
    slip.setStatus(Payroll.PayrollStatus.GENERATED);
    attendanceTools.getMonthlyAttendanceSummary(employeeId, month, year);
    return salarySlipRepository.save(slip);
  }

  @Tool(description = "Mark salary slip as paid")
  public Payroll markSalarySlipAsPaid(Long employeeId, int month, int year) {
    Payroll slip = getSalarySlip(employeeId, month, year);
    slip.setStatus(Payroll.PayrollStatus.PAID);
    return salarySlipRepository.save(slip);
  }

  @Tool(description = "Get salary slip")
  public Payroll getSalarySlip(Long employeeId, int month, int year) {
    return salarySlipRepository
        .findByEmployeeIdAndMonthAndYear(employeeId, month, year)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "No salary slip found for employee "
                        + employeeId
                        + " for "
                        + month
                        + "/"
                        + year));
  }

  @Tool(description = "Calculate gross salary")
  public double calculateGrossSalary(double basicSalary, double allowances, double bonus) {
    return basicSalary + allowances + bonus;
  }

  @Tool(description = "Calculate income tax using a configurable simple effective rate")
  public double calculateTax(double taxableIncome, double taxRatePercent) {
    if (taxableIncome <= 0) return 0;
    return taxableIncome * Math.max(taxRatePercent, 0) / 100.0;
  }

  @Tool(description = "Calculate total deductions")
  public double calculateDeductions(
      double tax, double providentFund, double insurance, double otherDeductions) {
    return Math.max(0, tax)
        + Math.max(0, providentFund)
        + Math.max(0, insurance)
        + Math.max(0, otherDeductions);
  }

  @Tool(description = "Calculate overtime pay")
  public double calculateOvertimePay(
      double overtimeHours, double hourlyRate, double overtimeMultiplier) {
    return Math.max(0, overtimeHours) * Math.max(0, hourlyRate) * Math.max(0, overtimeMultiplier);
  }

  @Tool(description = "Calculate unpaid leave deduction")
  public double calculateLeaveDeduction(double unpaidLeaveDays, double perDayRate) {
    return Math.max(0, unpaidLeaveDays) * Math.max(0, perDayRate);
  }

  @Tool(description = "Calculate bonus")
  public double calculateBonus(double basicSalary, double bonusPercent) {
    return Math.max(0, basicSalary) * Math.max(0, bonusPercent) / 100.0;
  }

  @Tool(
      description = "Calculate total reimbursement across expense categories for payroll inclusion")
  public double calculatePayrollReimbursement(
      double travel, double meals, double medical, double other) {
    return Math.max(0, travel) + Math.max(0, meals) + Math.max(0, medical) + Math.max(0, other);
  }

  @Tool(description = "Calculate net salary")
  public double calculateNetSalary(double grossSalary, double deductions) {
    return grossSalary - Math.max(0, deductions);
  }

  @Tool(description = "Get payroll history for an employee")
  public List<Payroll> getPayrollHistory(Long employeeId) {
    return salarySlipRepository.findAll().stream()
        .filter(p -> employeeId.equals(p.getEmployeeId()))
        .toList();
  }

  @Tool(description = "Get payroll summary for an employee and month")
  public PayrollSummary getPayrollSummary(Long employeeId, int month, int year) {
    Payroll p = getSalarySlip(employeeId, month, year);
    return new PayrollSummary(
        employeeId,
        month,
        year,
        p.getBasicSalary(),
        p.getAllowances(),
        p.getDeductions(),
        p.getUnpaidLeaveDeduction(),
        p.getNetSalary(),
        p.getStatus().name());
  }

  @Tool(description = "Generate payroll report for an employee and month")
  public PayrollReport generatePayrollReport(Long employeeId, int month, int year) {
    return new PayrollReport(getPayrollSummary(employeeId, month, year));
  }

  @Tool(description = "Explain salary calculation")
  public String explainSalary(Long employeeId, int month, int year) {
    Payroll p = getSalarySlip(employeeId, month, year);
    return "Gross components: basic="
        + p.getBasicSalary()
        + ", allowances="
        + p.getAllowances()
        + ". Deductions="
        + p.getDeductions()
        + ", including unpaid-leave deduction="
        + p.getUnpaidLeaveDeduction()
        + ". Net salary="
        + p.getNetSalary()
        + ".";
  }

  public record PayrollSummary(
      Long employeeId,
      int month,
      int year,
      double basicSalary,
      double allowances,
      double deductions,
      double unpaidLeaveDeduction,
      double netSalary,
      String status) {}

  public record PayrollReport(PayrollSummary summary) {}
}
