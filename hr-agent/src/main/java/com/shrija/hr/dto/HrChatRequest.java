package com.shrija.hr.dto;

import jakarta.validation.constraints.NotBlank;

public record HrChatRequest(@NotBlank String userId, String sessionId, @NotBlank String message) {}
