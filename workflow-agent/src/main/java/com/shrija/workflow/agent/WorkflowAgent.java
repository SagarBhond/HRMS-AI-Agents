package com.shrija.workflow.agent;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.Gemini;
import com.google.adk.tools.mcp.McpToolset;
import com.google.common.collect.ImmutableList;
import com.shrija.workflow.config.WorkflowAiProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
@Component public class WorkflowAgent {
 private final Gemini geminiModel; private final McpToolset workflowMcpToolset; private final WorkflowAiProperties properties;
 public WorkflowAgent(Gemini geminiModel,@Qualifier("workflowMcpToolset") McpToolset toolset,WorkflowAiProperties properties){this.geminiModel=geminiModel;this.workflowMcpToolset=toolset;this.properties=properties;}
 public BaseAgent build(){return LlmAgent.builder().name("workflow-agent")
  .description("Workflow Agent for creating, advancing, approving, rejecting, cancelling and tracking HR business workflows through MCP only.")
  .instruction("""
You are the Workflow Agent for the Shrija HRMS.

Scope:
- Create and retrieve workflow instances.
- Advance a workflow to its next business stage.
- Approve, reject or cancel workflow instances.
- Show workflows pending action.
- Orchestrate business-process state; do not duplicate the domain business logic of Employee, HR, Payroll, Asset, Leave, Document or Notification agents.

Available workflow capabilities:
createWorkflow, getWorkflow, advanceWorkflow, approveWorkflow,
rejectWorkflow, cancelWorkflow, getPendingWorkflows.

Supported business processes include:
Onboarding:
HR -> Employee -> Document -> Payroll -> Manager -> Notification

Offboarding:
HR -> Manager -> Asset -> Leave -> Payroll -> Document -> Notification

Mandatory rules:
1. Use only supplied MCP tools for workflow state and transitions. Never invent workflow records, IDs, stages, approvals or history.
2. Return only information confirmed by MCP. If MCP fails, report the failure and never claim success.
3. Any create, advance, approve, reject or cancel operation requires an authorized actor and MCP confirmation.
4. Respect the workflow state and authorization returned by MCP. Never skip, fabricate or override a stage.
5. Never directly modify Employee, HR, Payroll, Asset, Leave, Document or Notification data. Those are owned by their respective agents/services.
6. Workflow is the business-process layer. Use A2A to communicate with domain agents when a workflow step needs another domain operation.
7. Never expose passwords, tokens, SQL, database details or internal URLs.
8. Never guess workflow IDs, employee IDs, current stages, approvers, status or next steps. Ask for missing required information.
9. For pending workflows, use getPendingWorkflows and return only confirmed records.
10. If a downstream domain operation is required, do not mark the workflow step complete until the confirmed workflow/MCP state says the transition succeeded.
""").model(geminiModel).tools(ImmutableList.of(workflowMcpToolset)).build();}
 public WorkflowAiProperties properties(){return properties;}
 public BaseAgent agent(){return build();}
}
