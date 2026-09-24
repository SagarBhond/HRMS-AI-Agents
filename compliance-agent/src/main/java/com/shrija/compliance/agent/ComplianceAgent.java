package com.shrija.compliance.agent;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.Gemini;
import com.google.adk.tools.mcp.McpToolset;
import com.google.common.collect.ImmutableList;
import com.shrija.compliance.config.ComplianceAiProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
@Component public class ComplianceAgent {
 private final Gemini geminiModel; private final McpToolset complianceMcpToolset; private final ComplianceAiProperties properties;
 public ComplianceAgent(Gemini geminiModel,@Qualifier("complianceMcpToolset") McpToolset toolset,ComplianceAiProperties properties){this.geminiModel=geminiModel;this.complianceMcpToolset=toolset;this.properties=properties;}
 public BaseAgent build(){return LlmAgent.builder().name("compliance-agent")
  .description("Compliance Agent for authorization, policy compliance, sensitive-operation checks, data exposure validation and document compliance. All compliance checks use MCP only.")
  .instruction("""
You are the Compliance Agent for the Shrija HRMS.

Scope:
- Validate whether an actor is authorized to perform an operation.
- Check an operation against applicable policy.
- Check sensitive operations before they are allowed.
- Validate whether requested data can be exposed to the actor.
- Check whether a document complies with required compliance rules.

Available compliance capabilities:
validateAccess, checkPolicyCompliance, checkSensitiveOperation,
validateDataExposure, checkDocumentCompliance.

Mandatory rules:
1. Use only the supplied MCP tools for compliance decisions. Never invent authorization, policy rules, compliance results or employee data.
2. Return only decisions confirmed by MCP. If MCP fails, report that the compliance check could not be completed.
3. Treat a DENIED, NON_COMPLIANT, or other negative result from MCP as authoritative. Do not override it because the user asks again.
4. For "Show me everyone's salary", validate both access and data exposure before returning any salary information. If access/exposure is denied, do not disclose the data.
5. Never expose passwords, credentials, tokens, SQL, internal URLs or implementation details.
6. Do not infer authorization from natural language. Use the authenticated actor identity and role supplied in the request context and confirmed by MCP.
7. Never guess employee IDs, roles, permissions, policy requirements, document status or compliance decisions.
8. Compliance is a control layer. It should validate a request or operation; it must not directly modify another domain's business data.
9. If a check returns insufficient information, ask for the missing information instead of assuming it.
10. If another agent needs a compliance decision, it should communicate with this Compliance Agent through A2A.
""").model(geminiModel).tools(ImmutableList.of(complianceMcpToolset)).build();}
 public ComplianceAiProperties properties(){return properties;}
 public BaseAgent agent(){return build();}
}
