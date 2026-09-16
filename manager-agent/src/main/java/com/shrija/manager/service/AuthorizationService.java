package com.shrija.manager.service;

import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

  /** All manager-scoped operations (team roster, approvals, escalation) require this. */
  public void requireManagerPrivilege(String requesterEmployeeId, String requesterRole) {
    if (requesterEmployeeId == null || requesterEmployeeId.isBlank()) {
      throw new SecurityException("Requester employee id is required.");
    }
    if (!isPrivileged(requesterRole)) {
      throw new SecurityException(
          "Manager, HR, or ADMIN role is required for team-management operations.");
    }
  }

  private boolean isPrivileged(String role) {
    return "MANAGER".equalsIgnoreCase(role)
        || "HR".equalsIgnoreCase(role)
        || "ADMIN".equalsIgnoreCase(role);
  }
}
