package com.shrija.expense.tool;

import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Documents the business capabilities exposed to the Expense Agent.
 * Runtime execution is intentionally provided by the MCP server via expenseMcpToolset.
 */
@Component
public class ExpenseToolCatalog {
  public List<String> operations() {
    return List.of("submitExpense", "getExpense", "listExpenses", "validateExpense", "approveExpense", "rejectExpense", "calculateReimbursement", "getReimbursementStatus");
  }
}
