package com.shrija.orchestrator.controller;

import com.shrija.orchestrator.dto.OrchestratorChatRequest;
import com.shrija.orchestrator.dto.OrchestratorChatResponse;
import com.shrija.orchestrator.security.AuthenticatedUser;
import com.shrija.orchestrator.service.OrchestratorConversationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The single entry point the React UI calls after storing its JWT, matching the program flow: "ADK
 * Service :8080 -> JWT Authentication Filter -> AuthenticatedUser -> Orchestration Agent".
 */
@RestController
@RequestMapping("/api/v1/orchestrator")
public class OrchestratorController {

  private final OrchestratorConversationService conversationService;

  public OrchestratorController(OrchestratorConversationService conversationService) {
    this.conversationService = conversationService;
  }

  @PostMapping("/chat")
  public ResponseEntity<OrchestratorChatResponse> chat(
      @Valid @RequestBody OrchestratorChatRequest request, HttpServletRequest servletRequest) {
    AuthenticatedUser authenticatedUser =
        (AuthenticatedUser) servletRequest.getAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE);
    if (authenticatedUser == null) {
      // Defensive: JwtAuthenticationFilter should already have rejected the request before it
      // reaches here. This should be unreachable in practice.
      throw new IllegalStateException("Authenticated user context is missing.");
    }
    var result = conversationService.converse(authenticatedUser, request);
    return ResponseEntity.ok(
        new OrchestratorChatResponse(result.sessionId(), result.responseText()));
  }
}
