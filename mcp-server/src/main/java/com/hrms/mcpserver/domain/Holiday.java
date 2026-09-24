package com.hrms.mcpserver.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Owned by the Leave Agent. Company-wide (or region-specific) holiday calendar. */
@Entity
@Table(name = "holiday")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Holiday {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private LocalDate date;

  @Column(nullable = false)
  private String name;

  /** Optional — null means it applies to all locations. */
  private String region;
}
