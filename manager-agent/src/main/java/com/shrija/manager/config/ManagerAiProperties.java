package com.shrija.manager.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "shrija.ai")
@Validated
public record ManagerAiProperties(
    @NotBlank String geminiApiKey,
    @NotBlank String geminiModel,
    @NotBlank String mcpServerUrl,
    @NotBlank String employeeAgentUrl,
    @NotBlank String attendanceAgentUrl,
    @NotBlank String leaveAgentUrl,
    @NotBlank String expenseAgentUrl,
    @NotBlank String performanceAgentUrl) {}
