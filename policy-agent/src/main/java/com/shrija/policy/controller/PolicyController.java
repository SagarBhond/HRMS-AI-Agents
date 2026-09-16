package com.shrija.policy.controller;

import com.shrija.policy.dto.PolicyChatRequest;
import com.shrija.policy.dto.PolicyChatResponse;
import com.shrija.policy.service.PolicyConversationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/policy")
public class PolicyController {
  private final PolicyConversationService conversationService;

  public PolicyController(PolicyConversationService conversationService) {
    this.conversationService = conversationService;
  }

  @PostMapping("/chat")
  public ResponseEntity<PolicyChatResponse> chat(@Valid @RequestBody PolicyChatRequest request) {
    var result = conversationService.converse(request);
    return ResponseEntity.ok(new PolicyChatResponse(result.sessionId(), result.responseText()));
  }
}
