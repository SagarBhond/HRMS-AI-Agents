package com.shrija.expense.controller;

import com.shrija.expense.dto.ExpenseChatRequest;
import com.shrija.expense.dto.ExpenseChatResponse;
import com.shrija.expense.service.ExpenseConversationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/expense")
public class ExpenseController {
  private final ExpenseConversationService conversationService;
  public ExpenseController(ExpenseConversationService conversationService) { this.conversationService = conversationService; }
  @PostMapping("/chat")
  public ResponseEntity<ExpenseChatResponse> chat(@Valid @RequestBody ExpenseChatRequest request) {
    var result = conversationService.converse(request);
    return ResponseEntity.ok(new ExpenseChatResponse(result.sessionId(), result.responseText()));
  }
}
