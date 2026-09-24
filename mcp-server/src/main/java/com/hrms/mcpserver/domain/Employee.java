package com.hrms.mcpserver.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Owned by the Employee Agent. Core employee profile record — the single source of truth every
 * other agent (Leave, Attendance, Payroll, HR) links back to via employeeId.
 */
@Entity
@Table(name = "employee")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long employeeId;

  @Column(nullable = false)
  private String firstName;

  private String lastName;

  @Column(unique = true, nullable = false)
  private String email;

  private String department;

  private String designation;

  private Long managerEmployeeId;

  private LocalDate dateOfJoining;

  @Enumerated(EnumType.STRING)
  private EmployeeStatus status;

  public enum EmployeeStatus {
    ACTIVE,
    ON_LEAVE,
    SUSPENDED,
    TERMINATED
  }
}
