package com.shrija.employee.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "shrija.employee.ai")
public record EmployeeAiProperties(
    @NotBlank String geminiApiKey,
    @NotBlank String geminiModel,
    @NotBlank String mcpServerUrl,
    String attendanceAgentUrl,
    String leaveAgentUrl,
    String payrollAgentUrl,
    String managerAgentUrl) {}
