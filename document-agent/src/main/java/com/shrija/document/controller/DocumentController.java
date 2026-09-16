package com.shrija.document.controller;

import com.shrija.document.dto.DocumentChatRequest;
import com.shrija.document.dto.DocumentChatResponse;
import com.shrija.document.service.DocumentConversationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/document")
public class DocumentController {
  private final DocumentConversationService conversationService;

  public DocumentController(DocumentConversationService conversationService) {
    this.conversationService = conversationService;
  }

  @PostMapping("/chat")
  public ResponseEntity<DocumentChatResponse> chat(
      @Valid @RequestBody DocumentChatRequest request) {
    DocumentConversationService.Result result = conversationService.converse(request);
    return ResponseEntity.ok(new DocumentChatResponse(result.sessionId(), result.responseText()));
  }
}
