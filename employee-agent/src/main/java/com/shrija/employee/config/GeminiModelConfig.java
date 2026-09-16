package com.shrija.employee.config;

import com.google.adk.models.Gemini;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiModelConfig {

  @Bean
  public Gemini employeeGeminiModel(EmployeeAiProperties properties) {
    return new Gemini(properties.geminiModel(), properties.geminiApiKey());
  }
}
