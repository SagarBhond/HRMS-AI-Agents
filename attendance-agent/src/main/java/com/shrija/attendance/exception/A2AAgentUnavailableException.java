package com.shrija.attendance.exception;

/**
 * Raised when a downstream A2A peer agent (Employee, Manager, Payroll, Orchestrator) cannot be
 * reached or fails during a call.
 *
 * <p>Deliberately extends {@link IllegalStateException} so the existing {@code
 * AttendanceAgentExceptionHandler} handling of {@code IllegalStateException} (503) keeps working
 * exactly as before, and any other code relying on that type is unaffected. The target URL and the
 * underlying failure are kept for logging only ({@link #agentName()}, {@link #targetUrl()}) and are
 * never placed in the client-facing message.
 */
public class A2AAgentUnavailableException extends IllegalStateException {

  private final String agentName;
  private final String targetUrl;

  public A2AAgentUnavailableException(String agentName, String targetUrl, Throwable cause) {
    super(
        (agentName == null || agentName.isBlank() ? "A2A agent" : agentName)
            + " could not be reached",
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
