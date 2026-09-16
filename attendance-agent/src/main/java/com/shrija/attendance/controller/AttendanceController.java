package com.shrija.attendance.controller;

import com.shrija.attendance.dto.AttendanceChatRequest;
import com.shrija.attendance.dto.AttendanceChatResponse;
import com.shrija.attendance.service.AttendanceConversationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

  private final AttendanceConversationService conversationService;

  public AttendanceController(AttendanceConversationService conversationService) {
    this.conversationService = conversationService;
  }

  @PostMapping("/chat")
  public ResponseEntity<AttendanceChatResponse> chat(
          @Valid @RequestBody AttendanceChatRequest request) {
    var result = conversationService.converse(request);
    return ResponseEntity.ok(new AttendanceChatResponse(result.sessionId(), result.responseText()));
  }
}
