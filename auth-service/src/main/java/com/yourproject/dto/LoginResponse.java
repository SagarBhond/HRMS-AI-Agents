package com.yourproject.dto;

public class LoginResponse {

  private Long userId;
  private String username;
  private String role;
  private Long
      employeeId; // links straight into the MCP server's employee/attendance/payroll tables
  private String message;
  private String token; // JWT the frontend/Orchestration Agent will carry on later requests

  public LoginResponse(
      Long userId, String username, String role, Long employeeId, String message, String token) {
    this.userId = userId;
    this.username = username;
    this.role = role;
    this.employeeId = employeeId;
    this.message = message;
    this.token = token;
  }

  public Long getUserId() {
    return userId;
  }

  public String getUsername() {
    return username;
  }

  public String getRole() {
    return role;
  }

  public Long getEmployeeId() {
    return employeeId;
  }

  public String getMessage() {
    return message;
  }

  public String getToken() {
    return token;
  }
}
