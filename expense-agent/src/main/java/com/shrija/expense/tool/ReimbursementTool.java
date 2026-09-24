package com.shrija.expense.tool;

import com.google.common.collect.ImmutableList;
import java.util.List;

/** Groups reimbursement calculation/status capabilities owned by Expense. */
public final class ReimbursementTool {
  private ReimbursementTool() {}

  public static final String CALCULATE_REIMBURSEMENT = "calculateReimbursement";

  public static List<String> toolNames() {
    return ImmutableList.of(CALCULATE_REIMBURSEMENT);
  }
}
