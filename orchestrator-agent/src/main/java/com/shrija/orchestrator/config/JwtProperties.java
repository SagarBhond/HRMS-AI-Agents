package com.shrija.orchestrator.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Shared secret used to verify the HS256 JWT issued by the Auth Service (:8081). Claim names are
 * configurable because we don't control the Auth Service's token layout from this module; the
 * defaults match the Auth Service's userId, username, role, and employeeId claims.
 */
@Validated
@ConfigurationProperties(prefix = "shrija.orchestrator.jwt")
public record JwtProperties(
    @NotBlank String secret,
    String userIdClaim,
    String usernameClaim,
    String roleClaim,
    String employeeCodeClaim) {

  public String userIdClaimOrDefault() {
    return (userIdClaim == null || userIdClaim.isBlank()) ? "userId" : userIdClaim;
  }

  public String usernameClaimOrDefault() {
    return (usernameClaim == null || usernameClaim.isBlank()) ? "username" : usernameClaim;
  }

  public String roleClaimOrDefault() {
    return (roleClaim == null || roleClaim.isBlank()) ? "role" : roleClaim;
  }

  public String employeeCodeClaimOrDefault() {
    return (employeeCodeClaim == null || employeeCodeClaim.isBlank())
        ? "employeeId"
        : employeeCodeClaim;
  }
}
