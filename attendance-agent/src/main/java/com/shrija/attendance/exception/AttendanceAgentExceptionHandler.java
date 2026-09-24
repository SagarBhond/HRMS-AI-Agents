package com.shrija.attendance.exception;

import com.shrija.attendance.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Single place where every Attendance Agent REST failure is turned into a stable JSON body.
 *
 * <p>Extends {@link ResponseEntityExceptionHandler} so Spring MVC's own exceptions (validation,
 * unreadable body, unsupported method/media type) keep their correct HTTP status instead of being
 * swallowed by a catch-all {@code Exception} handler and returned as 500. The previous version of
 * this class only handled {@code AttendanceAgentExecutionException}, {@code SecurityException},
 * {@code IllegalStateException} and a generic {@code Exception} fallback; those four outcomes are
 * preserved (same status codes) with everything else layered on top.
 *
 * <p>This class only shapes error responses. It does not touch the agent, the MCP toolset, the A2A
 * wiring, {@code AuthorizationService}, or the {@code /api/v1/attendance/chat} success path.
 */
@RestControllerAdvice
public class AttendanceAgentExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger log =
      LoggerFactory.getLogger(AttendanceAgentExceptionHandler.class);

  private static final String GENERIC_MESSAGE =
      "Attendance request failed. Please retry or contact support with the traceId.";

  // ---------------------------------------------------------------------
  // Application exceptions
  // ---------------------------------------------------------------------

  /** Caller is not allowed to read/modify the requested attendance data. Same 403 as before. */
  @ExceptionHandler(SecurityException.class)
  public ResponseEntity<ErrorResponse> handleSecurity(
      SecurityException ex, HttpServletRequest request) {
    String traceId = newTraceId();
    log.warn("[{}] Authorization failure on {}", traceId, path(request), ex);
    return build(
        traceId,
        HttpStatus.FORBIDDEN,
        "Forbidden",
        safeMessage(ex, "You are not authorized to perform this operation."),
        path(request));
  }

  /** Bad caller input that reached the service layer (blank ids, unusable arguments). */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(
      IllegalArgumentException ex, HttpServletRequest request) {
    String traceId = newTraceId();
    log.warn("[{}] Invalid request argument on {}", traceId, path(request), ex);
    return build(
        traceId,
        HttpStatus.BAD_REQUEST,
        "Bad Request",
        safeMessage(ex, "Request contained an invalid value."),
        path(request));
  }

  /** Method-level {@code @Validated} constraint violations. */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest request) {
    String traceId = newTraceId();
    List<String> details = new ArrayList<>();
    if (ex.getConstraintViolations() != null) {
      for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
        details.add(violation.getPropertyPath() + ": " + violation.getMessage());
      }
    }
    log.warn("[{}] Constraint violation on {}: {}", traceId, path(request), details);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ErrorResponse.of(
                traceId,
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Request validation failed.",
                path(request),
                details));
  }

  /** A downstream A2A peer agent (Employee/Manager/Payroll/Orchestrator) could not be reached. */
  @ExceptionHandler(A2AAgentUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleA2AUnavailable(
      A2AAgentUnavailableException ex, HttpServletRequest request) {
    String traceId = newTraceId();
    // The target URL is internal topology: log it, never return it.
    log.error(
        "[{}] A2A peer unavailable agent={} url={} on {}",
        traceId,
        ex.agentName(),
        ex.targetUrl(),
        path(request),
        ex);
    return build(
        traceId,
        HttpStatus.SERVICE_UNAVAILABLE,
        "Service Unavailable",
        "A required downstream agent is currently unavailable. Please retry shortly.",
        path(request));
  }

  /**
   * MCP / Gemini / session state problems surfaced as {@code IllegalStateException}. Same 503 the
   * previous handler already returned for this case.
   */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleService(
      IllegalStateException ex, HttpServletRequest request) {
    String traceId = newTraceId();
    log.warn("[{}] Dependency unavailable on {}", traceId, path(request), ex);
    return build(
        traceId,
        HttpStatus.SERVICE_UNAVAILABLE,
        "Service Unavailable",
        "A required Attendance Agent dependency is currently unavailable. Please retry shortly.",
        path(request));
  }

  /** The ADK runner / Gemini call failed. Same 500 the previous handler already returned. */
  @ExceptionHandler(AttendanceAgentExecutionException.class)
  public ResponseEntity<ErrorResponse> handleAgentExecution(
      AttendanceAgentExecutionException ex, HttpServletRequest request) {
    String traceId = newTraceId();
    log.error("[{}] Attendance agent execution failed on {}", traceId, path(request), ex);
    return build(
        traceId,
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Internal Server Error",
        safeMessage(ex, "Failed to execute attendance agent."),
        path(request));
  }

  /** Request thread was interrupted while waiting on the agent or an A2A call. */
  @ExceptionHandler(InterruptedException.class)
  public ResponseEntity<ErrorResponse> handleInterrupted(
      InterruptedException ex, HttpServletRequest request) {
    Thread.currentThread().interrupt();
    String traceId = newTraceId();
    log.error("[{}] Attendance Agent request interrupted on {}", traceId, path(request), ex);
    return build(
        traceId,
        HttpStatus.SERVICE_UNAVAILABLE,
        "Service Unavailable",
        "Attendance Agent request was interrupted. Please retry.",
        path(request));
  }

  /** Last resort. Same 500 the previous catch-all returned; never leaks message/stack trace. */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
    String traceId = newTraceId();
    log.error("[{}] Unexpected error handling request on {}", traceId, path(request), ex);
    return build(
        traceId,
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Internal Server Error",
        GENERIC_MESSAGE,
        path(request));
  }

  // ---------------------------------------------------------------------
  // Spring MVC exceptions (overridden so they keep their proper status)
  // ---------------------------------------------------------------------

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    String traceId = newTraceId();
    List<String> details = new ArrayList<>();
    for (ObjectError error : ex.getBindingResult().getAllErrors()) {
      if (error instanceof FieldError fieldError) {
        details.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
      } else {
        details.add(error.getObjectName() + ": " + error.getDefaultMessage());
      }
    }
    log.warn("[{}] Request validation failed on {}: {}", traceId, path(request), details);
    ErrorResponse body =
        ErrorResponse.of(
            traceId,
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            "Request validation failed.",
            path(request),
            details);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).body(body);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    String traceId = newTraceId();
    log.warn("[{}] Unreadable request body on {}", traceId, path(request), ex);
    ErrorResponse body =
        ErrorResponse.of(
            traceId,
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            "Request body is missing or not valid JSON.",
            path(request));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).body(body);
  }

  /**
   * Shapes every remaining Spring MVC exception (405, 415, 404 on a missing handler, ...) into the
   * same {@link ErrorResponse} without altering the status Spring already chose.
   */
  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception ex,
      Object body,
      HttpHeaders headers,
      HttpStatusCode statusCode,
      WebRequest request) {
    if (body instanceof ErrorResponse) {
      return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }
    String traceId = newTraceId();
    HttpStatus status = HttpStatus.resolve(statusCode.value());
    log.warn("[{}] {} on {}", traceId, statusCode.value(), path(request), ex);
    ErrorResponse errorBody =
        ErrorResponse.of(
            traceId,
            statusCode.value(),
            status == null ? "Error" : status.getReasonPhrase(),
            safeMessage(ex, "Request could not be processed."),
            path(request));
    return super.handleExceptionInternal(ex, errorBody, headers, statusCode, request);
  }

  // ---------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------

  private ResponseEntity<ErrorResponse> build(
      String traceId, HttpStatus status, String error, String message, String path) {
    return ResponseEntity.status(status)
        .body(ErrorResponse.of(traceId, status.value(), error, message, path));
  }

  /** {@code getMessage()} is frequently null (NPEs, wrapped Rx errors) — never pass it through raw. */
  private static String safeMessage(Throwable ex, String fallback) {
    if (ex == null) {
      return fallback;
    }
    String message = ex.getMessage();
    return message == null || message.isBlank() ? fallback : message;
  }

  private static String newTraceId() {
    return UUID.randomUUID().toString().substring(0, 8);
  }

  private static String path(HttpServletRequest request) {
    return request == null ? null : request.getRequestURI();
  }

  private static String path(WebRequest request) {
    if (request instanceof ServletWebRequest servletWebRequest) {
      return servletWebRequest.getRequest().getRequestURI();
    }
    return request == null ? null : request.getDescription(false);
  }
}
