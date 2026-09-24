package com.shrija.manager.controller;

import com.shrija.manager.dto.ManagerChatRequest;
import com.shrija.manager.dto.ManagerChatResponse;
import com.shrija.manager.service.ManagerConversationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/manager")
public class ManagerController {

  private final ManagerConversationService conversationService;

  public ManagerController(ManagerConversationService conversationService) {
    this.conversationService = conversationService;
  }

  @PostMapping("/chat")
  public ResponseEntity<ManagerChatResponse> chat(@Valid @RequestBody ManagerChatRequest request) {
    var result =
        conversationService.converse(request.userId(), request.sessionId(), request.message());
    return ResponseEntity.ok(new ManagerChatResponse(result.sessionId(), result.responseText()));
  }
}
