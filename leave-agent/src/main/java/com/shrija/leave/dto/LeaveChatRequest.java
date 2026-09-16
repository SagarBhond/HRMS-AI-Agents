package com.shrija.leave.dto;

import jakarta.validation.constraints.NotBlank;

public record LeaveChatRequest(@NotBlank String userId, String sessionId, @NotBlank String message) {}
