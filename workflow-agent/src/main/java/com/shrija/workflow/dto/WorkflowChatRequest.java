package com.shrija.workflow.dto; import jakarta.validation.constraints.NotBlank; public record WorkflowChatRequest(@NotBlank String message,String sessionId,String userId,String role) {}
