package com.shrija.compliance.dto; import jakarta.validation.constraints.NotBlank; public record ComplianceChatRequest(@NotBlank String message,String sessionId,String userId,String role) {}
