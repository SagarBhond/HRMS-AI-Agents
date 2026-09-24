package com.yourproject.controller;

import com.yourproject.dto.CreateUserRequest;
import com.yourproject.dto.LoginRequest;
import com.yourproject.dto.LoginResponse;
import com.yourproject.dto.UserResponse;
import com.yourproject.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${auth.cors.allowed-origin:http://localhost:3000}")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    return authService.login(request);
  }

  @PostMapping("/users")
  public ResponseEntity<UserResponse> createUser(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
          String authorizationHeader,
      @Valid @RequestBody CreateUserRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(authService.createUser(authorizationHeader, request));
  }

  @GetMapping("/me")
  public UserResponse currentUser(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
          String authorizationHeader) {
    return authService.currentUser(authorizationHeader);
  }

  @PatchMapping("/users/{userId}/link-employee/{employeeId}")
  public ResponseEntity<Void> linkEmployee(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
          String authorizationHeader,
      @PathVariable Long userId,
      @PathVariable Long employeeId) {
    authService.linkEmployee(authorizationHeader, userId, employeeId);
    return ResponseEntity.noContent().build();
  }
}
