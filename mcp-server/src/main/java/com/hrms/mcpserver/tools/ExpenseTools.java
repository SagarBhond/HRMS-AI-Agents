package com.hrms.mcpserver.tools;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class ExpenseTools {
  private final AtomicLong ids = new AtomicLong(1);
  private final Map<Long, ExpenseRecord> expenses = new ConcurrentHashMap<>();
  private final NotificationTools notificationTools;

  public ExpenseTools(NotificationTools notificationTools) {
    this.notificationTools = notificationTools;
  }

  @Tool(description = "Submit an employee expense")
  public ExpenseRecord submitExpense(Long employeeId, String category, double amount, String description) {
    long id = ids.getAndIncrement();
    ExpenseRecord e = new ExpenseRecord(id, employeeId, category, amount, description, "SUBMITTED", null, Instant.now().toString());
    expenses.put(id, e); return e;
  }

  @Tool(description = "Get an expense")
  public ExpenseRecord getExpense(Long expenseId) {
    ExpenseRecord e = expenses.get(expenseId);
    if (e == null) throw new IllegalArgumentException("No expense found with id " + expenseId);
    return e;
  }

  @Tool(description = "List employee expenses")
  public List<ExpenseRecord> listExpenses(Long employeeId) {
    return expenses.values().stream().filter(e -> employeeId == null || employeeId.equals(e.employeeId())).toList();
  }

  @Tool(description = "Validate an expense")
  public ValidationResult validateExpense(Long expenseId) {
    ExpenseRecord e = getExpense(expenseId);
    return new ValidationResult(expenseId, e.amount() > 0, e.amount() > 0 ? "Expense is valid" : "Amount must be positive");
  }

  @Tool(description = "Approve an expense")
  public ExpenseRecord approveExpense(Long expenseId, Long approverEmployeeId) {
    ExpenseRecord e = getExpense(expenseId);
    ExpenseRecord n = new ExpenseRecord(e.expenseId(), e.employeeId(), e.category(), e.amount(), e.description(), "APPROVED", approverEmployeeId, e.createdAt());
    expenses.put(expenseId, n); return n;
  }

  @Tool(description = "Reject an expense")
  public ExpenseRecord rejectExpense(Long expenseId, Long approverEmployeeId, String reason) {
    ExpenseRecord e = getExpense(expenseId);
    ExpenseRecord n = new ExpenseRecord(e.expenseId(), e.employeeId(), e.category(), e.amount(), e.description(), "REJECTED: " + reason, approverEmployeeId, e.createdAt());
    expenses.put(expenseId, n); return n;
  }

  @Tool(description = "Calculate reimbursement amount")
  public double calculateReimbursement(double amount, double reimbursablePercent) {
    return Math.max(0, amount) * Math.max(0, Math.min(reimbursablePercent, 100)) / 100.0;
  }

  @Tool(description = "Get reimbursement status")
  public String getReimbursementStatus(Long expenseId) { return getExpense(expenseId).status(); }

  @Tool(description = "Get an employee's full expense history")
  public List<ExpenseRecord> getExpenseHistory(Long employeeId) {
    return listExpenses(employeeId);
  }

  @Tool(description = "Generate an expense report for an employee, or all employees if null")
  public ExpenseReport generateExpenseReport(Long employeeId) {
    List<ExpenseRecord> records = listExpenses(employeeId);
    double total = records.stream().mapToDouble(ExpenseRecord::amount).sum();
    long approved = records.stream().filter(e -> "APPROVED".equals(e.status())).count();
    long pending = records.stream().filter(e -> "SUBMITTED".equals(e.status())).count();
    return new ExpenseReport(employeeId, records.size(), approved, pending, total);
  }

  @Tool(description = "Forward an approved expense to Payroll for reimbursement in the employee's next salary run")
  public NotificationTools.NotificationRecord sendExpenseToPayroll(Long expenseId) {
    ExpenseRecord e = getExpense(expenseId);
    if (!"APPROVED".equals(e.status())) {
      throw new IllegalStateException("Only approved expenses can be sent to payroll");
    }
    return notificationTools.sendPayrollNotification(
        "Expense reimbursement due",
        "Expense " + expenseId + " for employee " + e.employeeId() + " (" + e.amount() + ") approved and ready for payroll reimbursement.");
  }

  public record ExpenseRecord(Long expenseId, Long employeeId, String category, double amount, String description, String status, Long approverEmployeeId, String createdAt) {}
  public record ValidationResult(Long expenseId, boolean valid, String message) {}
  public record ExpenseReport(Long employeeId, int totalExpenses, long approvedCount, long pendingCount, double totalAmount) {}
}
