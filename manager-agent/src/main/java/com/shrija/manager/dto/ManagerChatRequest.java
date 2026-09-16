package com.shrija.manager.dto;

import jakarta.validation.constraints.NotBlank;

public record ManagerChatRequest(String userId, String sessionId, @NotBlank String message) {}
