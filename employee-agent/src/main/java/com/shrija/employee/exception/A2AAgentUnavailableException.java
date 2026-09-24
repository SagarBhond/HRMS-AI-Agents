package com.shrija.employee.exception;

/**
 * Raised when a downstream A2A peer agent (Attendance, Leave, Payroll, Manager) cannot be resolved
 * or reached.
 *
 * <p>Deliberately extends {@link IllegalStateException} so that any existing caller catching {@code
 * IllegalStateException} keeps working exactly as before. The target URL is kept in {@link
 * #targetUrl()} for logging only and is never placed in the client-facing message.
 */
public class A2AAgentUnavailableException extends IllegalStateException {

  private final String agentName;
  private final String targetUrl;

  public A2AAgentUnavailableException(String agentName, String targetUrl, Throwable cause) {
    super(
        (agentName == null || agentName.isBlank() ? "A2A agent" : agentName) + " is unavailable",
        cause);
    this.agentName = agentName;
    this.targetUrl = targetUrl;
  }

  public String agentName() {
    return agentName;
  }

  public String targetUrl() {
    return targetUrl;
  }
}
