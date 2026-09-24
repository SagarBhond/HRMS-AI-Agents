package com.shrija.employee.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

/**
 * Uniform error payload returned by every failed Employee Agent request.
 *
 * <p>Null fields are omitted from the JSON, so a simple failure stays compact while a validation
 * failure can carry per-field details.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    String timestamp,
    String traceId,
    int status,
    String error,
    String message,
    String path,
    List<String> details) {

  public static ErrorResponse of(
      String traceId, int status, String error, String message, String path) {
    return new ErrorResponse(
        Instant.now().toString(), traceId, status, error, message, path, null);
  }

  public static ErrorResponse of(
      String traceId,
      int status,
      String error,
      String message,
      String path,
      List<String> details) {
    return new ErrorResponse(
        Instant.now().toString(),
        traceId,
        status,
        error,
        message,
        path,
        details == null || details.isEmpty() ? null : List.copyOf(details));
  }
}
