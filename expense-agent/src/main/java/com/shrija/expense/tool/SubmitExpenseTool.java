package com.shrija.expense.tool;

import com.google.common.collect.ImmutableList;
import java.util.List;

/** Groups expense submission capabilities owned by the Expense Agent. */
public final class SubmitExpenseTool {
  private SubmitExpenseTool() {}

  public static final String SUBMIT_EXPENSE = "submitExpense";

  public static List<String> toolNames() {
    return ImmutableList.of(SUBMIT_EXPENSE);
  }
}
