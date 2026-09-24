package com.yourproject.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "auth_user")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String username;

  // Stores a BCrypt hash, never plain text
  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  // Links this login to a row in the MCP server's own `employee` table
  // (same physical hrms_db, different service's entity model — so this stays
  // a plain column here rather than a JPA @ManyToOne to an Employee class).
  // Nullable: a login can exist before HR creates the matching employee record,
  // or belong to a system/admin account with no employee profile at all.
  @Column(name = "employee_id", unique = true)
  private Long employeeId;

  public User() {}

  public User(String username, String password, Role role) {
    this.username = username;
    this.password = password;
    this.role = role;
  }

  public User(String username, String password, Role role, Long employeeId) {
    this.username = username;
    this.password = password;
    this.role = role;
    this.employeeId = employeeId;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public Long getEmployeeId() {
    return employeeId;
  }

  public void setEmployeeId(Long employeeId) {
    this.employeeId = employeeId;
  }
}
