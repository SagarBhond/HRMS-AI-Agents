package com.shrija.notification.controller;

import com.shrija.notification.dto.NotificationChatRequest;
import com.shrija.notification.dto.NotificationChatResponse;
import com.shrija.notification.service.NotificationConversationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notification")
public class NotificationController {
  private final NotificationConversationService conversationService;

  public NotificationController(NotificationConversationService conversationService) {
    this.conversationService = conversationService;
  }

  @PostMapping("/chat")
  public ResponseEntity<NotificationChatResponse> chat(
      @Valid @RequestBody NotificationChatRequest request) {
    NotificationConversationService.Result result = conversationService.converse(request);
    return ResponseEntity.ok(
        new NotificationChatResponse(result.sessionId(), result.responseText()));
  }
}
