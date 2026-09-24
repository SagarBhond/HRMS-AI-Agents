package com.shrija.performance.dto;
import jakarta.validation.constraints.NotBlank;
public record PerformanceChatRequest(@NotBlank String userId,String sessionId,@NotBlank String role,@NotBlank String message) {}
