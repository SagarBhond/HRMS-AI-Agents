package com.hrms.mcpserver.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Owned by the HR Agent — "Employee lifecycle". Tracks onboarding, transfers, promotions, exits,
 * etc. HR calls the Payroll Agent (to trigger final settlement / revised CTC) and the Budget Agent
 * (to check headcount cost) as part of these events.
 */
@Entity
@Table(name = "hr")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HR {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long eventId;

  @Column(nullable = false)
  private Long employeeId;

  @Enumerated(EnumType.STRING)
  private LifecycleEventType eventType;

  private LocalDate eventDate;

  private String details;

  public enum LifecycleEventType {
    ONBOARDING,
    PROMOTION,
    TRANSFER,
    DESIGNATION_CHANGE,
    EXIT
  }
}
