package com.yourproject.controller;

import com.yourproject.dto.ErrorResponse;
import com.yourproject.service.AccountProvisioningException;
import com.yourproject.service.DuplicateUserException;
import com.yourproject.service.ForbiddenOperationException;
import com.yourproject.service.InvalidCredentialsException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ErrorResponse> unauthorized(InvalidCredentialsException ex) {
    return response(HttpStatus.UNAUTHORIZED, ex.getMessage());
  }

  @ExceptionHandler(ForbiddenOperationException.class)
  public ResponseEntity<ErrorResponse> forbidden(ForbiddenOperationException ex) {
    return response(HttpStatus.FORBIDDEN, ex.getMessage());
  }

  @ExceptionHandler(DuplicateUserException.class)
  public ResponseEntity<ErrorResponse> conflict(DuplicateUserException ex) {
    return response(HttpStatus.CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(AccountProvisioningException.class)
  public ResponseEntity<ErrorResponse> provisioningFailed(AccountProvisioningException ex) {
    return response(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> invalidRequest(MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
    return response(HttpStatus.BAD_REQUEST, message);
  }

  private ResponseEntity<ErrorResponse> response(HttpStatus status, String message) {
    return ResponseEntity.status(status).body(new ErrorResponse(message));
  }
}
