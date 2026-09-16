package com.shrija.audit.config;

import com.google.adk.models.Gemini;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiModelConfig {
  @Bean
  public Gemini geminiModel(AuditAiProperties properties) {
    return Gemini.builder()
        .apiKey(properties.geminiApiKey())
        .modelName(properties.geminiModel())
        .build();
  }
}
