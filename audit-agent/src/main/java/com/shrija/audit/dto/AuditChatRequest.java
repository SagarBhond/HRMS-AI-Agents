package com.shrija.audit.dto; import jakarta.validation.constraints.NotBlank; public record AuditChatRequest(@NotBlank String message,String sessionId,String userId,String role) {}
