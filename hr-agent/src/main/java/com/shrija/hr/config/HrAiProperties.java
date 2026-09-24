package com.shrija.hr.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "shrija.ai")
@Validated
public record HrAiProperties(
    @NotBlank String geminiApiKey,
    @NotBlank String geminiModel,
    @NotBlank String mcpServerUrl,
    @NotBlank String payrollAgentUrl,
    String budgetAgentUrl,
    String documentAgentUrl,
    String orchestrationAgentUrl,
    String employeeAgentUrl,
    String managerAgentUrl,
    String recruitmentAgentUrl) {}
