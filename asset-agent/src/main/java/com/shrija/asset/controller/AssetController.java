package com.shrija.asset.controller;

import com.shrija.asset.dto.AssetChatRequest;
import com.shrija.asset.dto.AssetChatResponse;
import com.shrija.asset.service.AssetConversationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/asset")
public class AssetController {
  private final AssetConversationService conversationService;
  public AssetController(AssetConversationService conversationService) { this.conversationService = conversationService; }
  @PostMapping("/chat")
  public ResponseEntity<AssetChatResponse> chat(@Valid @RequestBody AssetChatRequest request) {
    var result = conversationService.converse(request);
    return ResponseEntity.ok(new AssetChatResponse(result.sessionId(), result.responseText()));
  }
}
