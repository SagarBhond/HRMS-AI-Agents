package com.shrija.employee.controller;

import com.shrija.employee.dto.EmployeeChatRequest;
import com.shrija.employee.dto.EmployeeChatResponse;
import com.shrija.employee.service.EmployeeConversationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/employee")
public class EmployeeController {

  private final EmployeeConversationService conversationService;

  public EmployeeController(EmployeeConversationService conversationService) {
    this.conversationService = conversationService;
  }

  @PostMapping("/chat")
  public ResponseEntity<EmployeeChatResponse> chat(
      @Valid @RequestBody EmployeeChatRequest request) {
    EmployeeConversationService.Result result = conversationService.converse(request);
    return ResponseEntity.ok(new EmployeeChatResponse(result.sessionId(), result.responseText()));
  }
}
