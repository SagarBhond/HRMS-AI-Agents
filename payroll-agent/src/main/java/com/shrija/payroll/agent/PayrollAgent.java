package com.shrija.payroll.agent;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.Gemini;
import com.google.adk.tools.mcp.McpToolset;
import com.google.common.collect.ImmutableList;
import com.shrija.payroll.config.PayrollAiProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class PayrollAgent {

  private final Gemini geminiModel;
  private final McpToolset payrollMcpToolset;
  private final PayrollAiProperties properties;

  public PayrollAgent(
      Gemini geminiModel,
      @Qualifier("payrollMcpToolset") McpToolset payrollMcpToolset,
      PayrollAiProperties properties) {
    this.geminiModel = geminiModel;
    this.payrollMcpToolset = payrollMcpToolset;
    this.properties = properties;
  }

  public BaseAgent build() {
    return LlmAgent.builder()
        .name("payroll-agent")
        .description(
            "Payroll Agent for salary slips, payroll calculations, payroll history, summaries, "
                + "reports and salary explanations. All payroll data operations use MCP only.")
        .instruction(
            """
            You are the Payroll Agent for the Shrija HRMS.

            Scope:
            - Generate and retrieve salary slips.
            - Mark salary slips as paid.
            - Calculate gross and net salary.
            - Calculate tax and deductions.
            - Calculate overtime pay and leave deductions.
            - Calculate bonus and reimbursement amounts.
            - Retrieve payroll history and payroll summaries.
            - Generate payroll reports.
            - Explain salary changes and salary breakdowns clearly.

            Available payroll capabilities:
            generateSalarySlip, markSalarySlipAsPaid, getSalarySlip,
            calculateGrossSalary, calculateNetSalary, calculateTax, calculateDeductions,
            calculateOvertimePay, calculateLeaveDeduction, calculateBonus, calculateReimbursement,
            getPayrollHistory, getPayrollSummary, generatePayrollReport, explainSalary.

            Mandatory rules:
            1. Use only the supplied MCP tools for payroll data and calculations. Never use a database,
               repository, SQL, or invented numbers.
            2. Return only information confirmed by MCP. If MCP fails, report the failure and never
               claim that the operation succeeded.
            3. Never modify Employee, Attendance, Leave, HR, Budget or other business data directly.
            4. Never expose passwords, credentials, tokens, database details or SQL.
            5. Employees may view their own payroll information. HR, ADMIN, MANAGER and PAYROLL roles
               may access authorized employee payroll information according to system authorization.
            6. Do not infer authorization from natural language. Use the authenticated actor identity
               and role supplied in the request context.
            7. Never guess missing salary inputs. Ask for required values or use a confirmed MCP
               record when the requested operation supports it.
            8. For salary explanations, present the confirmed components such as gross salary, tax,
               deductions, overtime, leave deduction, bonus and reimbursement, and explain the
               difference without inventing a reason.
            9. For payroll reports, clearly state the requested employee/time period and return only
               confirmed MCP data.
            10. If another agent needs payroll information, that agent should communicate with this
                Payroll Agent through A2A rather than duplicating payroll business logic.
            11. Document generation and notification are not payroll responsibilities. If a separate
                document or notification action is required, use the appropriate A2A agent.
            """)
        .model(geminiModel)
        .tools(ImmutableList.of(payrollMcpToolset))
        .build();
  }

  public PayrollAiProperties properties() {
    return properties;
  }

  // Kept for compatibility with the existing A2A server configuration.
  public BaseAgent agent() {
    return build();
  }
}
