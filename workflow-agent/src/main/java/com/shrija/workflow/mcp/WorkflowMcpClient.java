package com.shrija.workflow.mcp;
import com.google.adk.JsonBaseModel;
import com.google.adk.tools.mcp.McpToolset;
import com.google.adk.tools.mcp.SseServerParameters;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.springframework.stereotype.Component;
@Component public class WorkflowMcpClient {
 private static final List<String> ALLOWED_TOOLS=ImmutableList.of(
  "createWorkflow","getWorkflow","advanceWorkflow","approveWorkflow","rejectWorkflow","cancelWorkflow","getPendingWorkflows");
 public McpToolset createToolset(String url){return new McpToolset(SseServerParameters.builder().url(url).build(),JsonBaseModel.getMapper(),ALLOWED_TOOLS);}
 public List<String> allowedTools(){return ALLOWED_TOOLS;}
}
