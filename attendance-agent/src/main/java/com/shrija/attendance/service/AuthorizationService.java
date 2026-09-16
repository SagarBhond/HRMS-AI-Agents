package com.shrija.attendance.service;

import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

  public void requireSelfOrPrivileged(
      String requesterEmployeeId, String requesterRole, String targetEmployeeId) {
    require(requesterEmployeeId, requesterRole, targetEmployeeId);
    if (!requesterEmployeeId.equals(targetEmployeeId) && !isPrivileged(requesterRole)) {
      throw new SecurityException(
          "You are not authorized to access another employee's attendance.");
    }
  }

  public void requirePrivileged(String requesterEmployeeId, String requesterRole) {
    if (requesterEmployeeId == null || requesterEmployeeId.isBlank()) {
      throw new SecurityException("Requester employee id is required.");
    }
    if (!isPrivileged(requesterRole)) {
      throw new SecurityException("Manager, HR, or ADMIN role is required for team attendance.");
    }
  }

  private void require(String requesterEmployeeId, String requesterRole, String targetEmployeeId) {
    if (requesterEmployeeId == null
        || requesterEmployeeId.isBlank()
        || requesterRole == null
        || requesterRole.isBlank()
        || targetEmployeeId == null
        || targetEmployeeId.isBlank()) {
      throw new SecurityException("Requester identity, role, and target employee id are required.");
    }
  }

  private boolean isPrivileged(String role) {
    return "MANAGER".equalsIgnoreCase(role)
        || "HR".equalsIgnoreCase(role)
        || "ADMIN".equalsIgnoreCase(role);
  }
}
