package com.shrija.leave.exception;

import com.shrija.leave.controller.LeaveController;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = LeaveController.class)
public class LeaveAgentExceptionHandler {

  @ExceptionHandler(SecurityException.class)
  public ResponseEntity<Map<String, Object>> handleSecurity(SecurityException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(Map.of("timestamp", Instant.now().toString(), "error", ex.getMessage()));
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, Object>> handleService(IllegalStateException ex) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(Map.of("timestamp", Instant.now().toString(), "error", ex.getMessage()));
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, Object>> handleAgentExecution(RuntimeException ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            Map.of(
                "timestamp",
                Instant.now().toString(),
                "error",
                "Leave Agent execution failed.",
                "message",
                ex.getMessage() != null
                    ? ex.getMessage()
                    : "Unknown Leave Agent execution error."));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            Map.of(
                "timestamp",
                Instant.now().toString(),
                "error",
                "Leave request failed.",
                "message",
                ex.getMessage() != null ? ex.getMessage() : "Unknown error."));
  }
}
