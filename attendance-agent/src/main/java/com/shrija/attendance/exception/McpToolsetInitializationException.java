package com.shrija.attendance.exception;

/**
 * Raised when the Attendance MCP toolset cannot be constructed (bad/blank MCP URL, SSE handshake
 * failure, etc.).
 *
 * <p>Extends {@link IllegalStateException} so Spring's existing bean-creation failure behaviour is
 * unchanged; it only makes the cause obvious in the startup log instead of surfacing a bare
 * constructor exception.
 */
public class McpToolsetInitializationException extends IllegalStateException {

  public McpToolsetInitializationException(String message, Throwable cause) {
    super(message, cause);
  }
}
