package com.shrija.orchestrator.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * The Orchestrator Agent uses no MCP server of its own (per the responsibility table: "Uses MCP: No
 * (indirectly through agents)") — it only needs a model and the A2A URL of every downstream agent
 * it may route to.
 */
@Validated
@ConfigurationProperties(prefix = "shrija.orchestrator.ai")
public record OrchestratorAiProperties(
    @NotBlank String geminiApiKey,
    @NotBlank String geminiModel,
    String employeeAgentUrl,
    String attendanceAgentUrl,
    String payrollAgentUrl,
    String hrAgentUrl,
    String leaveAgentUrl,
    String managerAgentUrl,
    String documentAgentUrl,
    String notificationAgentUrl,
    String policyAgentUrl,
    String expenseAgentUrl,
    String assetAgentUrl,
    String performanceAgentUrl,
    String recruitmentAgentUrl,
    String auditAgentUrl,
    String complianceAgentUrl,
    String workflowAgentUrl) {}
