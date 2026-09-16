package com.shrija.document.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "document-ai")
public record DocumentAiProperties(
    @NotBlank String geminiApiKey,
    @NotBlank String geminiModel,
    @NotBlank String mcpServerUrl,
    String employeeAgentUrl,
    String hrAgentUrl,
    String payrollAgentUrl,
    String leaveAgentUrl,
    String recruitmentAgentUrl,
    String performanceAgentUrl,
    String complianceAgentUrl,
    String workflowAgentUrl) {}
