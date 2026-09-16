package com.shrija.expense.tool;

import com.google.common.collect.ImmutableList;
import java.util.List;

/** Groups expense reporting and downstream payroll handoff capabilities. */
public final class ExpenseReportTool {
  private ExpenseReportTool() {}

  public static final String GENERATE_EXPENSE_REPORT = "generateExpenseReport";
  public static final String SEND_EXPENSE_TO_PAYROLL = "sendExpenseToPayroll";

  public static List<String> toolNames() {
    return ImmutableList.of(GENERATE_EXPENSE_REPORT, SEND_EXPENSE_TO_PAYROLL);
  }
}
