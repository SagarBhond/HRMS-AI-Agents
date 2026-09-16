package com.shrija.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record NotificationChatRequest(
    String sessionId, @NotBlank String userId, @NotBlank String role, @NotBlank String message) {}
