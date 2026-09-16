package com.shrija.policy.dto;

import jakarta.validation.constraints.NotBlank;

public record PolicyChatRequest(
    @NotBlank String userId, String sessionId, @NotBlank String role, @NotBlank String message) {}
