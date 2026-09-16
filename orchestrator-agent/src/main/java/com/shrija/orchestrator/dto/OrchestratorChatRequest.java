package com.shrija.orchestrator.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Deliberately carries only sessionId + message. userId/role/employeeCode are NEVER accepted from
 * the request body -- they come only from the verified JWT (see AuthenticatedUser), so a client
 * cannot claim a different identity or role than the one the Auth Service issued.
 */
public record OrchestratorChatRequest(String sessionId, @NotBlank String message) {}
