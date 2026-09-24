package com.shrija.attendance.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "shrija.ai")
@Validated
public record AttendanceAiProperties(
    @NotBlank String geminiApiKey,
    @NotBlank String geminiModel,
    @NotBlank String mcpServerUrl,
    @NotBlank String employeeAgentUrl,
    @NotBlank String payrollAgentUrl,
    @NotBlank String managerAgentUrl,
    String orchestratorAgentUrl) {}
