package com.shrija.attendance.config;

import com.google.adk.models.Gemini;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiModelConfig {

  @Bean
  public Gemini geminiModel(AttendanceAiProperties properties) {
    return new Gemini(properties.geminiModel(), properties.geminiApiKey());
  }
}
