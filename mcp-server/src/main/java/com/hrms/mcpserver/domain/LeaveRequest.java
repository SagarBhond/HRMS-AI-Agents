package com.hrms.mcpserver.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Owned by the Leave Agent — "Leave requests and balances". The Leave Agent is called by Manager
 * (for approvals), HR (lifecycle checks) and Payroll (unpaid-leave deductions).
 */
@Entity
@Table(name = "leave_request")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long leaveRequestId;

  @Column(nullable = false)
  private Long employeeId;

  @Enumerated(EnumType.STRING)
  private LeaveType leaveType;

  private LocalDate startDate;

  private LocalDate endDate;

  @Enumerated(EnumType.STRING)
  private LeaveStatus status;

  private String approvedByManagerId;

  private String reason;

  public enum LeaveType {
    SICK,
    CASUAL,
    EARNED,
    UNPAID,
    MATERNITY,
    PATERNITY
  }

  public enum LeaveStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED
  }
}
