package com.shrija.orchestrator.config;

import com.google.adk.models.Gemini;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiModelConfig {

  @Bean
  public Gemini orchestratorGeminiModel(OrchestratorAiProperties properties) {
    return new Gemini(properties.geminiModel(), properties.geminiApiKey());
  }
}
