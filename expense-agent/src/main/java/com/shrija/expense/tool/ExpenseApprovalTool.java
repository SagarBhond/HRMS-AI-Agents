package com.shrija.expense.tool;

import com.google.common.collect.ImmutableList;
import java.util.List;

/** Groups expense approval and rejection capabilities. */
public final class ExpenseApprovalTool {
  private ExpenseApprovalTool() {}

  public static final String APPROVE_EXPENSE = "approveExpense";
  public static final String REJECT_EXPENSE = "rejectExpense";

  public static List<String> toolNames() {
    return ImmutableList.of(APPROVE_EXPENSE, REJECT_EXPENSE);
  }
}
