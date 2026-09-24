package com.shrija.workflow.agent;
/** Documents the workflow capability boundary. Actual execution is provided by the shared MCP server. */
public final class WorkflowTool {
 private WorkflowTool(){}
 public static final String[] FUNCTIONS={"createWorkflow","getWorkflow","advanceWorkflow","approveWorkflow","rejectWorkflow","cancelWorkflow","getPendingWorkflows"};
}
