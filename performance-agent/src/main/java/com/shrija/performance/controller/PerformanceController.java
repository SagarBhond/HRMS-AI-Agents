package com.shrija.performance.controller;
import com.shrija.performance.dto.PerformanceChatRequest;
import com.shrija.performance.dto.PerformanceChatResponse;
import com.shrija.performance.service.PerformanceConversationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/performance")
public class PerformanceController {
  private final PerformanceConversationService conversationService;
  public PerformanceController(PerformanceConversationService conversationService){this.conversationService=conversationService;}
  @PostMapping("/chat")
  public ResponseEntity<PerformanceChatResponse> chat(@Valid @RequestBody PerformanceChatRequest request){
    var result=conversationService.converse(request);
    return ResponseEntity.ok(new PerformanceChatResponse(result.sessionId(),result.responseText()));
  }
}
