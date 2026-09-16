package com.shrija.document.agent;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.Gemini;
import com.google.adk.tools.mcp.McpToolset;
import com.google.common.collect.ImmutableList;
import com.shrija.document.config.DocumentAiProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class DocumentAgent {
  private final Gemini geminiModel;
  private final McpToolset documentMcpToolset;
  private final DocumentAiProperties properties;

  public DocumentAgent(
      Gemini geminiModel,
      @Qualifier("documentMcpToolset") McpToolset documentMcpToolset,
      DocumentAiProperties properties) {
    this.geminiModel = geminiModel;
    this.documentMcpToolset = documentMcpToolset;
    this.properties = properties;
  }

  public BaseAgent build() {
    return LlmAgent.builder()
        .name("document-agent")
        .description("Creates, generates, stores and retrieves HRMS documents through MCP.")
        .instruction("""
            You are the Document Agent for Shrija AI HRMS.

            Responsibilities:
            - Create professional HR documents from confirmed business data.
            - Generate PDF and DOCX documents.
            - Store and retrieve generated documents.
            - List documents belonging to an employee.
            - Support HRMS employment and payroll-related document requests.

            Mandatory rules:
            1. Use only the supplied MCP tools for document operations.
            2. Never invent employee, HR, payroll, leave, attendance or salary facts.
            3. Verify employee-specific information before generating an official document.
            4. Never access a database or repository directly.
            5. Never expose credentials, JWTs, SQL, database details or internal URLs.
            6. This agent creates/manages documents; it does not perform HR business mutations.
            7. Use A2A when information is needed from Employee, HR, Payroll or Leave agents.
            8. Ask for missing mandatory information instead of guessing.
            9. Never claim generation/storage success unless MCP confirms it.
            10. Return concise business-friendly results, not raw MCP/A2A payloads.
            """)
        .model(geminiModel)
        .tools(ImmutableList.of(documentMcpToolset))
        .build();
  }

  public DocumentAiProperties properties() {
    return properties;
  }
}
