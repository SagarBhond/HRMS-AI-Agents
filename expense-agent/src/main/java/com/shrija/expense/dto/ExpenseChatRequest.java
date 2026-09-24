package com.shrija.expense.dto;

import jakarta.validation.constraints.NotBlank;

public record ExpenseChatRequest(@NotBlank String userId, String sessionId, @NotBlank String role, @NotBlank String message) {}
