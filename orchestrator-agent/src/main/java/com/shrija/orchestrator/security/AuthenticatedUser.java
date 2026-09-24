package com.shrija.orchestrator.security;

/**
 * Mirrors the "AuthenticatedUser" object in the program flow: userId, username, role, employeeCode
 * extracted from the verified JWT. Never populated from anything the client sends in the request
 * body — always from the token the JwtAuthenticationFilter has already verified.
 */
public record AuthenticatedUser(String userId, String username, String role, String employeeCode) {

  public static final String REQUEST_ATTRIBUTE = "shrija.authenticatedUser";
}
