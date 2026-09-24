package com.shrija.attendance.exception;

/**
 * Raised when the Attendance Agent (ADK runner / Gemini / MCP tool call) fails to produce a
 * response.
 *
 * <p>Unchanged behaviour: still an unchecked {@link RuntimeException} carrying the original cause.
 * A message-only constructor was added for failures that have no underlying throwable.
 */
public class AttendanceAgentExecutionException extends RuntimeException {

    public AttendanceAgentExecutionException(String message) {
        super(message);
    }

    public AttendanceAgentExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}