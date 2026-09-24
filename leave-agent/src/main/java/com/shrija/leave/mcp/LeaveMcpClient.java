package com.shrija.leave.mcp;

import com.google.adk.JsonBaseModel;
import com.google.adk.tools.mcp.McpToolset;
import com.google.adk.tools.mcp.SseServerParameters;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LeaveMcpClient {

  private static final List<String> ALLOWED_TOOLS =
      ImmutableList.of(
          "applyForLeave",
          "decideOnLeaveRequest",
          "getLeaveBalances",
          "getLeaveRequests",
          "cancelLeave",
          "modifyLeaveRequest",
          "getLeaveHistory",
          "checkLeaveEligibility",
          "checkLeaveOverlap",
          "calculateLeaveDuration",
          "checkHolidayConflict",
          "checkWeekendConflict",
          "getLeaveCalendar",
          "getTeamLeaveCalendar");

  public McpToolset createToolset(String mcpServerUrl) {
    return new McpToolset(
        SseServerParameters.builder().url(mcpServerUrl).build(),
        JsonBaseModel.getMapper(),
        ALLOWED_TOOLS);
  }

  public List<String> allowedTools() {
    return ALLOWED_TOOLS;
  }
}
