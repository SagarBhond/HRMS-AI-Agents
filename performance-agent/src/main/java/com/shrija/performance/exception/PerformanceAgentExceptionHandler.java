package com.shrija.performance.exception;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice
public class PerformanceAgentExceptionHandler {
  @ExceptionHandler(PerformanceAgentExecutionException.class)
  public ResponseEntity<Map<String,Object>> handleExecution(PerformanceAgentExecutionException ex){
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("timestamp",Instant.now().toString(),"error",ex.getMessage()));
  }
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String,Object>> handleService(IllegalStateException ex){
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("timestamp",Instant.now().toString(),"error",ex.getMessage()));
  }
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String,Object>> handleUnexpected(Exception ex){
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("timestamp",Instant.now().toString(),"error","Performance request failed."));
  }
}
