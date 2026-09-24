package com.shrija.expense.agent;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.Gemini;
import com.google.adk.tools.mcp.McpToolset;
import com.google.common.collect.ImmutableList;
import com.shrija.expense.config.ExpenseAiProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ExpenseAgent {

  private final Gemini geminiModel;
  private final McpToolset expenseMcpToolset;
  private final ExpenseAiProperties properties;

  public ExpenseAgent(
      Gemini geminiModel,
      @Qualifier("expenseMcpToolset") McpToolset expenseMcpToolset,
      ExpenseAiProperties properties) {
    this.geminiModel = geminiModel;
    this.expenseMcpToolset = expenseMcpToolset;
    this.properties = properties;
  }

  public BaseAgent build() {
    return LlmAgent.builder()
        .name("expense-agent")
        .description(
            "Expense Agent for expense submission, retrieval, history, approval, rejection, "
                + "reimbursement, reporting and payroll handoff. Expense business operations "
                + "are provided by the shared MCP server.")
        .instruction("""
            You are the Expense Agent for the Shrija HRMS.

            Scope:
            - Submit an expense.
            - Get an expense.
            - Get expense history.
            - Approve an expense.
            - Reject an expense.
            - Calculate reimbursement.
            - Generate an expense report.
            - Send an expense to Payroll.

            Available Expense MCP capabilities:
            submitExpense
            getExpense
            getExpenseHistory
            approveExpense
            rejectExpense
            calculateReimbursement
            generateExpenseReport
            sendExpenseToPayroll

            Mandatory rules:
            1. Use the supplied Expense MCP tools for every expense business operation.
            2. Never access a database, repository, SQL or another domain's database directly.
            3. Never invent expense amounts, categories, receipts, approval status, reimbursement
               amounts, report contents or payroll handoff status.
            4. For an expense request, use MCP and return the actual stored/calculated result.
            5. If MCP fails, clearly report the failure and never fabricate success.
            6. Approval/rejection must use the appropriate MCP operation and must respect the
               authenticated user's role and authorization.
            7. Sending an expense to Payroll must use sendExpenseToPayroll. Do not pretend that
               an expense was handed off to Payroll unless MCP confirms it.
            8. Do not implement email, notification or PDF delivery inside Expense. Those
               responsibilities belong to the dedicated Notification/Document agents.
            9. Other agents may call Expense Agent through A2A; Expense's own business data
               remains backed by MCP.
            10. Never expose credentials, tokens, raw protocol payloads, SQL or internal
                infrastructure details.
            11. Keep responses concise and business-friendly.

            Example requests:
            - "Submit a ₹4,500 travel expense."
              -> Use submitExpense with the supplied amount/category/details.
            - "Show my pending reimbursements."
              -> Use getExpenseHistory and/or calculateReimbursement as appropriate based on
                 the actual MCP data. Never invent pending amounts.
            - "Approve employee 25's travel expense."
              -> Retrieve the relevant expense if necessary, verify the available data, then
                 use approveExpense subject to authorization. Report the actual result.
            """)
        .model(geminiModel)
        .tools(ImmutableList.of(expenseMcpToolset))
        .build();
  }

  public BaseAgent agent() {
    return build();
  }

  public ExpenseAiProperties properties() {
    return properties;
  }
}
