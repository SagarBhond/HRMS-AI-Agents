package com.hrms.mcpserver.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Owned by the Leave Agent. One row per employee per leave type per year. */
@Entity
@Table(
    name = "leave_balance",
    uniqueConstraints = @UniqueConstraint(columnNames = {"employeeId", "leaveType", "year"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalance {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long employeeId;

  @Enumerated(EnumType.STRING)
  private LeaveRequest.LeaveType leaveType;

  private int year;

  private double totalDays;

  private double usedDays;

  private double remainingDays;
}
