package com.shrija.employee.dto;

import jakarta.validation.constraints.NotBlank;

public record EmployeeChatRequest(
    @NotBlank String userId, String sessionId, @NotBlank String role, @NotBlank String message) {}
