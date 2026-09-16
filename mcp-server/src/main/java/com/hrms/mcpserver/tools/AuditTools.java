package com.hrms.mcpserver.tools;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Tools backing the Audit Agent — an append-only log of significant actions taken across every
 * other agent (who did what, to which employee, and when), plus lookups over that log.
 */
@Component
public class AuditTools {

  private final AtomicLong ids = new AtomicLong(1);
  private final Map<Long, AuditEvent> events = new ConcurrentHashMap<>();

  @Tool(description = "Record an audit event describing an action taken by an agent")
  public AuditEvent recordAuditEvent(
      @ToolParam(description = "Name of the agent that performed the action") String agentName,
      @ToolParam(description = "Action performed, e.g. APPROVE_LEAVE, GENERATE_PAYSLIP") String action,
      @ToolParam(description = "Employee ID the action relates to, or null") Long employeeId,
      @ToolParam(description = "Free-text details of the event") String details,
      @ToolParam(description = "true if this action touches sensitive data (salary, PII, terminations)")
          boolean sensitive) {
    long id = ids.getAndIncrement();
    AuditEvent event =
        new AuditEvent(id, agentName, action, employeeId, details, sensitive, Instant.now().toString());
    events.put(id, event);
    return event;
  }

  @Tool(description = "Get the full audit history, most recent first")
  public List<AuditEvent> getAuditHistory() {
    return events.values().stream()
        .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
        .toList();
  }

  @Tool(description = "Search audit events by agent name, action, and/or employee ID (any may be null)")
  public List<AuditEvent> searchAuditEvents(
      @ToolParam(description = "Agent name filter, or null") String agentName,
      @ToolParam(description = "Action filter, or null") String action,
      @ToolParam(description = "Employee ID filter, or null") Long employeeId) {
    return events.values().stream()
        .filter(e -> agentName == null || agentName.equalsIgnoreCase(e.agentName()))
        .filter(e -> action == null || action.equalsIgnoreCase(e.action()))
        .filter(e -> employeeId == null || employeeId.equals(e.employeeId()))
        .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
        .toList();
  }

  @Tool(description = "Get the audit history for a specific employee")
  public List<AuditEvent> getEmployeeAuditHistory(@ToolParam(description = "Employee ID") Long employeeId) {
    return searchAuditEvents(null, null, employeeId);
  }

  @Tool(description = "Get the audit history for a specific agent")
  public List<AuditEvent> getAgentAuditHistory(@ToolParam(description = "Agent name") String agentName) {
    return searchAuditEvents(agentName, null, null);
  }

  @Tool(description = "Get all audit events flagged as sensitive operations")
  public List<AuditEvent> getSensitiveOperations() {
    return events.values().stream()
        .filter(AuditEvent::sensitive)
        .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
        .toList();
  }

  public record AuditEvent(
      Long auditEventId,
      String agentName,
      String action,
      Long employeeId,
      String details,
      boolean sensitive,
      String createdAt) {}
}
