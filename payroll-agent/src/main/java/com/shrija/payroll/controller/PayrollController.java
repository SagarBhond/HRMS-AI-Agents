package com.shrija.payroll.controller;

import com.shrija.payroll.dto.PayrollChatRequest;
import com.shrija.payroll.dto.PayrollChatResponse;
import com.shrija.payroll.service.PayrollConversationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payroll")
public class PayrollController {

  private final PayrollConversationService conversationService;

  public PayrollController(PayrollConversationService conversationService) {
    this.conversationService = conversationService;
  }

  @PostMapping("/chat")
  public ResponseEntity<PayrollChatResponse> chat(@Valid @RequestBody PayrollChatRequest request) {
    var result = conversationService.converse(request);
    return ResponseEntity.ok(new PayrollChatResponse(result.sessionId(), result.responseText()));
  }
}
