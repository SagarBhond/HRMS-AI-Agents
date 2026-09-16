package com.shrija.hr.agent;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.Gemini;
import com.google.adk.tools.FunctionTool;
import com.google.adk.tools.mcp.McpToolset;
import com.google.common.collect.ImmutableList;
import com.shrija.hr.config.HrAiProperties;
import com.shrija.hr.tool.HrBudgetTool;
import com.shrija.hr.tool.HrDocumentTool;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class HrAgent {
  private final Gemini geminiModel; private final McpToolset hrMcpToolset;
  private final HrAiProperties properties; private final HrBudgetTool budgetTool; private final HrDocumentTool documentTool;
  public HrAgent(Gemini model,@Qualifier("hrMcpToolset") McpToolset toolset,HrAiProperties properties,
      HrBudgetTool budgetTool,HrDocumentTool documentTool){geminiModel=model;hrMcpToolset=toolset;this.properties=properties;this.budgetTool=budgetTool;this.documentTool=documentTool;}
  public BaseAgent build(){return LlmAgent.builder().name("hr-agent")
      .description("Main HR operations agent using MCP for HR/employee data and A2A for document and budget delegation.")
      .instruction("""
          You are the main HR Agent for the Shrija HRMS.
          MCP operations: getEmployeeProfile, createEmployee, updateEmployeeDetails, listEmployees,
          recordOnboarding, recordPromotion, recordTransfer, recordExit, getLifecycleHistory.
          A2A operations: sendHeadcountCostToBudget -> Budget Agent; createJoiningLetter, createOfferLetter,
          createPromotionLetter, createTransferLetter, createExperienceLetter, createRelievingLetter,
          createExitLetter, createSalaryRevisionLetter, createWarningLetter, createAppreciationLetter
          -> Document Agent.
          Rules: use MCP for HR/employee facts; never invent data; respect requester identity and role;
          do not modify other domains directly; never generate PDF/DOCX locally; document operations delegate
          through A2A; headcount/cost delegates through A2A; ask for missing data; report MCP/A2A failures.
          """)
      .model(geminiModel).tools(ImmutableList.of(hrMcpToolset,
          FunctionTool.create(budgetTool,"sendHeadcountCostToBudget"),
          FunctionTool.create(documentTool,"createJoiningLetter"),FunctionTool.create(documentTool,"createOfferLetter"),
          FunctionTool.create(documentTool,"createPromotionLetter"),FunctionTool.create(documentTool,"createTransferLetter"),
          FunctionTool.create(documentTool,"createExperienceLetter"),FunctionTool.create(documentTool,"createRelievingLetter"),
          FunctionTool.create(documentTool,"createExitLetter"),FunctionTool.create(documentTool,"createSalaryRevisionLetter"),
          FunctionTool.create(documentTool,"createWarningLetter"),FunctionTool.create(documentTool,"createAppreciationLetter"))).build();}
  public BaseAgent agent(){return build();}
  public HrAiProperties properties(){return properties;}
}
