package com.shrija.leave.controller;

import com.shrija.leave.dto.LeaveChatRequest;
import com.shrija.leave.dto.LeaveChatResponse;
import com.shrija.leave.service.LeaveConversationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/leave")
public class LeaveController {

  private final LeaveConversationService conversationService;

  public LeaveController(LeaveConversationService conversationService) {
    this.conversationService = conversationService;
  }

  @PostMapping("/chat")
  public ResponseEntity<LeaveChatResponse> chat(@Valid @RequestBody LeaveChatRequest request) {
    var result =
        conversationService.converse(request.userId(), request.sessionId(), request.message());
    return ResponseEntity.ok(new LeaveChatResponse(result.sessionId(), result.responseText()));
  }
}
