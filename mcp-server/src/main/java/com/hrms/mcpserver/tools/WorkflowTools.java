package com.hrms.mcpserver.tools;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/** Tools backing the Workflow Agent — generic multi-step approval workflows used across domains. */
@Component
public class WorkflowTools {

  private final AtomicLong ids = new AtomicLong(1);
  private final Map<Long, Workflow> workflows = new ConcurrentHashMap<>();

  @Tool(description = "Create a workflow")
  public Workflow createWorkflow(String name, String type, String description) {
    Workflow w = new Workflow(ids.getAndIncrement(), name, type, description, "CREATED", null, Instant.now().toString());
    workflows.put(w.workflowId(), w);
    return w;
  }

  @Tool(description = "Get a workflow by ID")
  public Workflow getWorkflow(Long workflowId) {
    return req(workflowId);
  }

  @Tool(description = "List all workflows")
  public List<Workflow> listWorkflows() {
    return workflows.values().stream().toList();
  }

  @Tool(description = "Advance a workflow to its next step")
  public Workflow advanceWorkflow(
      @ToolParam(description = "Workflow ID") Long workflowId,
      @ToolParam(description = "Name of the step being advanced to") String nextStep) {
    return setState(workflowId, "IN_PROGRESS", nextStep);
  }

  @Tool(description = "Approve the current step of a workflow")
  public Workflow approveWorkflow(
      @ToolParam(description = "Workflow ID") Long workflowId,
      @ToolParam(description = "Approver employee ID") Long approverEmployeeId) {
    Workflow w = req(workflowId);
    Workflow n = new Workflow(workflowId, w.name(), w.type(), w.description(), "APPROVED", w.currentStep(), w.createdAt());
    workflows.put(workflowId, n);
    return n;
  }

  @Tool(description = "Reject the current step of a workflow")
  public Workflow rejectWorkflow(
      @ToolParam(description = "Workflow ID") Long workflowId,
      @ToolParam(description = "Approver employee ID") Long approverEmployeeId,
      @ToolParam(description = "Reason for rejection") String reason) {
    Workflow w = req(workflowId);
    Workflow n = new Workflow(workflowId, w.name(), w.type(), w.description(), "REJECTED: " + reason, w.currentStep(), w.createdAt());
    workflows.put(workflowId, n);
    return n;
  }

  @Tool(description = "Cancel a workflow")
  public Workflow cancelWorkflow(Long workflowId) {
    return setState(workflowId, "CANCELLED", req(workflowId).currentStep());
  }

  @Tool(description = "Get all workflows that are pending action (CREATED or IN_PROGRESS)")
  public List<Workflow> getPendingWorkflows() {
    return workflows.values().stream()
        .filter(w -> "CREATED".equals(w.status()) || "IN_PROGRESS".equals(w.status()))
        .toList();
  }

  private Workflow setState(Long id, String status, String step) {
    Workflow w = req(id);
    Workflow n = new Workflow(id, w.name(), w.type(), w.description(), status, step, w.createdAt());
    workflows.put(id, n);
    return n;
  }

  private Workflow req(Long id) {
    Workflow w = workflows.get(id);
    if (w == null) throw new IllegalArgumentException("No workflow found with id " + id);
    return w;
  }

  public record Workflow(Long workflowId, String name, String type, String description, String status, String currentStep, String createdAt) {}
}
