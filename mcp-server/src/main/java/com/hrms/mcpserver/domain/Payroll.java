package com.hrms.mcpserver.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Owned by the Payroll Agent — "Salary generation". Built from data pulled from the Attendance
 * Agent (hours worked) and the Leave Agent (unpaid-leave days) via MCP tool calls.
 */
@Entity
@Table(
    name = "payroll",
    uniqueConstraints = @UniqueConstraint(columnNames = {"employeeId", "month", "year"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payroll {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long salarySlipId;

  @Column(nullable = false)
  private Long employeeId;

  private int month;

  private int year;

  private double basicSalary;

  private double allowances;

  private double deductions;

  private double unpaidLeaveDeduction;

  private double netSalary;

  @Enumerated(EnumType.STRING)
  private PayrollStatus status;

  public enum PayrollStatus {
    DRAFT,
    GENERATED,
    PAID
  }
}
