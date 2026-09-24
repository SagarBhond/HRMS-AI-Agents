package com.shrija.workflow.config;
import com.google.adk.models.Gemini;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration public class GeminiModelConfig {
 @Bean public Gemini workflowGeminiModel(WorkflowAiProperties p){return new Gemini(p.geminiModel(),p.geminiApiKey());}
}
