package com.shrija.payroll.dto;

import jakarta.validation.constraints.NotBlank;

public record PayrollChatRequest(
    @NotBlank String userId, String sessionId, @NotBlank String role, @NotBlank String message) {}
