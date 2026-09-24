package com.shrija.expense.tool;

import com.google.common.collect.ImmutableList;
import java.util.List;

/** Groups expense retrieval and history capabilities. */
public final class ExpenseHistoryTool {
  private ExpenseHistoryTool() {}

  public static final String GET_EXPENSE = "getExpense";
  public static final String GET_EXPENSE_HISTORY = "getExpenseHistory";

  public static List<String> toolNames() {
    return ImmutableList.of(GET_EXPENSE, GET_EXPENSE_HISTORY);
  }
}
