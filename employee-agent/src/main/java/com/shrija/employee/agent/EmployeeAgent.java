package com.shrija.employee.agent;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.Gemini;
import com.google.adk.tools.mcp.McpToolset;
import com.google.common.collect.ImmutableList;
import com.shrija.employee.config.EmployeeAiProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class EmployeeAgent {

  private final Gemini geminiModel;
  private final McpToolset employeeMcpToolset;
  private final EmployeeAiProperties properties;

  public EmployeeAgent(
      Gemini geminiModel,
      @Qualifier("employeeMcpToolset") McpToolset employeeMcpToolset,
      EmployeeAiProperties properties) {
    this.geminiModel = geminiModel;
    this.employeeMcpToolset = employeeMcpToolset;
    this.properties = properties;
  }

  public BaseAgent build() {
    return LlmAgent.builder()
        .name("employee-agent")
        .description(
            "Primary employee-information agent. Handles employee identity, profile, contact, "
                + "department, designation, manager and employment status through MCP only.")
        .instruction(
            """
            You are the Employee Agent for the Shrija HRMS.

            Scope:
            - Employee profile and contact information.
            - Employee ID, department, designation and employment status.
            - Employee profile, search and master-data operations.
            - Manager, department and employment-status lookup.
            - Employee self-service profile/contact operations.
            - Reporting hierarchy, documents, employment history and employee summary.

            Mandatory rules:
            1. Use only the supplied MCP tools for employee data. Never use a database, repository,
               SQL, or invented data.
            2. Return only information confirmed by MCP.
            3. Never modify Attendance, Leave, Payroll, Budget or HR business data.
            4. Never expose passwords, credentials, tokens, database details or SQL.
            5. Employees may read and update their own permitted profile/contact fields.
            6. Managers may access only authorized reporting-team information.
            7. HR/admin operations require an authorized HR/ADMIN actor.
            8. Do not infer authorization from the natural-language request. Use the actor identity
               and role supplied by the request context.
            9. For a missing employee or failed MCP operation, report the confirmed failure plainly.
            10. For create/update operations, validate required fields and never silently overwrite
                an existing employee.
            11. If another agent needs employee information, that agent should communicate with this
                Employee Agent through A2A; do not duplicate employee business logic elsewhere.
            """)
        .model(geminiModel)
        .tools(ImmutableList.of(employeeMcpToolset))
        .build();
  }

  public EmployeeAiProperties properties() {
    return properties;
  }
}
