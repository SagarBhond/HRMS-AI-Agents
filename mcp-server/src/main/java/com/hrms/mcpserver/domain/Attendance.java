package com.hrms.mcpserver.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Owned by the Attendance Agent — "Attendance and working hours". Consumed by the Payroll Agent
 * when generating salaries.
 */
@Entity
@Table(
    name = "attendance",
    uniqueConstraints = @UniqueConstraint(columnNames = {"employeeId", "workDate"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long attendanceId;

  @Column(nullable = false)
  private Long employeeId;

  private LocalDate workDate;

  private LocalTime checkIn;

  private LocalTime checkOut;

  private double hoursWorked;

  @Enumerated(EnumType.STRING)
  private AttendanceStatus status;

  public enum AttendanceStatus {
    PRESENT,
    ABSENT,
    HALF_DAY,
    ON_LEAVE,
    HOLIDAY,
    WEEK_OFF
  }
}
