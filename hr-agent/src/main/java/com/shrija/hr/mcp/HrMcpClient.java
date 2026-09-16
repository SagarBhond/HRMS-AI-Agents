package com.shrija.hr.mcp;

import com.google.adk.JsonBaseModel;
import com.google.adk.tools.mcp.McpToolset;
import com.google.adk.tools.mcp.SseServerParameters;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class HrMcpClient {
  private static final List<String> ALLOWED_TOOLS = ImmutableList.of(
      "getEmployeeProfile", "findEmployeeByEmail", "createEmployee", "updateEmployeeDetails",
      "listEmployees", "recordOnboarding", "recordPromotion", "recordTransfer", "recordExit",
      "getLifecycleHistory");
  public McpToolset createToolset(String mcpServerUrl) {
    return new McpToolset(SseServerParameters.builder().url(mcpServerUrl).build(),
        JsonBaseModel.getMapper(), ALLOWED_TOOLS);
  }
  public List<String> allowedTools() { return ALLOWED_TOOLS; }
}
