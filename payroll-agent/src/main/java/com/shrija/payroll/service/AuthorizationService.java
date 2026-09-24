package com.shrija.payroll.service;

import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

  public void requireSelfOrPrivileged(
      String requesterId, String requesterRole, String targetEmployeeId) {
    if (requesterId == null || requesterId.isBlank()) {
      throw new SecurityException("Requester identity is required.");
    }
    if (isPrivileged(requesterRole)) {
      return;
    }
    if (!requesterId.equalsIgnoreCase(targetEmployeeId)) {
      throw new SecurityException(
          "You are not authorized to access payroll data for employee " + targetEmployeeId);
    }
  }

  public void requirePrivileged(String requesterId, String requesterRole) {
    if (requesterId == null || requesterId.isBlank()) {
      throw new SecurityException("Requester identity is required.");
    }
    if (!isPrivileged(requesterRole)) {
      throw new SecurityException(
          "Only HR, Manager, Admin or Payroll roles may perform this payroll operation.");
    }
  }

  private boolean isPrivileged(String role) {
    if (role == null) {
      return false;
    }
    String r = role.trim().toUpperCase();
    return r.equals("HR") || r.equals("ADMIN") || r.equals("MANAGER") || r.equals("PAYROLL");
  }
}
