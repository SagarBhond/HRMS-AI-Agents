package com.shrija.asset.dto;

import jakarta.validation.constraints.NotBlank;

public record AssetChatRequest(@NotBlank String userId, String sessionId, @NotBlank String role, @NotBlank String message) {}
