package com.shrija.payroll.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "shrija.ai")
public record PayrollAiProperties(
    @NotBlank String geminiApiKey,
    @NotBlank String geminiModel,
    @NotBlank String mcpServerUrl,
    String employeeAgentUrl,
    String attendanceAgentUrl,
    String leaveAgentUrl,
    String managerAgentUrl,
    String hrAgentUrl) {}
