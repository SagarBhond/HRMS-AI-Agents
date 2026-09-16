package com.shrija.attendance.exception;

import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AttendanceAgentExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(AttendanceAgentExceptionHandler.class);

  @ExceptionHandler(SecurityException.class)
  public ResponseEntity<Map<String, Object>> handleSecurity(SecurityException ex) {
    log.warn("Security exception", ex);
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(Map.of("timestamp", Instant.now().toString(), "error", ex.getMessage()));
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, Object>> handleService(IllegalStateException ex) {
    log.warn("Service exception", ex);
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(Map.of("timestamp", Instant.now().toString(), "error", ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
    log.error("Unexpected error handling request", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("timestamp", Instant.now().toString(), "error", "Attendance request failed."));
  }
}
