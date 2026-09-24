package com.shrija.hr.controller;

import com.shrija.hr.dto.HrChatRequest;
import com.shrija.hr.dto.HrChatResponse;
import com.shrija.hr.service.HrConversationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hr")
public class HrController {

  private final HrConversationService conversationService;

  public HrController(HrConversationService conversationService) {
    this.conversationService = conversationService;
  }

  @PostMapping("/chat")
  public ResponseEntity<HrChatResponse> chat(@Valid @RequestBody HrChatRequest request) {
    var result =
        conversationService.converse(request.userId(), request.sessionId(), request.message());
    return ResponseEntity.ok(new HrChatResponse(result.sessionId(), result.responseText()));
  }
}
