package com.shrija.recruitment.dto; import jakarta.validation.constraints.NotBlank; public record RecruitmentChatRequest(@NotBlank String message,String sessionId,String userId,String role) {}
