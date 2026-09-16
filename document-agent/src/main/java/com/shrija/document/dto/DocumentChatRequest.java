package com.shrija.document.dto;

import jakarta.validation.constraints.NotBlank;

public record DocumentChatRequest(
    String sessionId,
    @NotBlank String userId,
    @NotBlank String role,
    @NotBlank String message) {}
