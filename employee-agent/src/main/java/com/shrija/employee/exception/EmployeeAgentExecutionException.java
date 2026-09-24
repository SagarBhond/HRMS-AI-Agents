package com.shrija.employee.exception;

/**
 * Raised when the Employee Agent (ADK runner / Gemini / MCP tool call) fails to produce a response.
 *
 * <p>Unchanged behaviour: still an unchecked {@link RuntimeException} carrying the original cause. A
 * message-only constructor was added for failures that have no underlying throwable.
 */
public class EmployeeAgentExecutionException extends RuntimeException {

  public EmployeeAgentExecutionException(String message) {
    super(message);
  }

  public EmployeeAgentExecutionException(String message, Throwable cause) {
    super(message, cause);
  }
}
