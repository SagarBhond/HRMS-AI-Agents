package com.shrija.leave.agent;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.Gemini;
import com.google.adk.tools.FunctionTool;
import com.google.adk.tools.mcp.McpToolset;
import com.google.common.collect.ImmutableList;
import com.shrija.leave.a2a.DocumentAgentClient;
import com.shrija.leave.a2a.NotificationAgentClient;
import com.shrija.leave.config.LeaveAiProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class LeaveAgent {

  private final Gemini geminiModel;
  private final McpToolset leaveMcpToolset;
  private final LeaveAiProperties properties;
  private final DocumentAgentClient documentAgentClient;
  private final NotificationAgentClient notificationAgentClient;

  public LeaveAgent(
      Gemini geminiModel,
      @Qualifier("leaveMcpToolset") McpToolset leaveMcpToolset,
      LeaveAiProperties properties,
      DocumentAgentClient documentAgentClient,
      NotificationAgentClient notificationAgentClient) {
    this.geminiModel = geminiModel;
    this.leaveMcpToolset = leaveMcpToolset;
    this.properties = properties;
    this.documentAgentClient = documentAgentClient;
    this.notificationAgentClient = notificationAgentClient;
  }

  public BaseAgent build() {
    return LlmAgent.builder()
        .name("leave-agent")
        .description(
            "Leave Agent for leave applications, approvals, balances, requests, "
                + "cancellations, modifications, eligibility, validations and leave calendars. "
                + "All Leave business operations use MCP. Document and notification work is "
                + "delegated to dedicated agents through A2A.")
        .instruction(
            """
                    You are the Leave Agent for the Shrija HRMS.

                    Scope:
                    - Apply for leave.
                    - Decide on leave requests.
                    - Get leave balances.
                    - Get leave requests.
                    - Cancel leave.
                    - Modify leave requests.
                    - Get leave history.
                    - Check leave eligibility.
                    - Check leave overlap.
                    - Calculate leave duration.
                    - Check holiday conflict.
                    - Check weekend conflict.
                    - Get leave calendar.
                    - Get team leave calendar.

                    Leave MCP capabilities:
                    applyForLeave, decideOnLeaveRequest, getLeaveBalances, getLeaveRequests,
                    cancelLeave, modifyLeaveRequest, getLeaveHistory, checkLeaveEligibility,
                    checkLeaveOverlap, calculateLeaveDuration, checkHolidayConflict,
                    checkWeekendConflict, getLeaveCalendar, getTeamLeaveCalendar.

                    A2A capabilities:
                    - Document Agent: create leave-related documents.
                    - Notification Agent: send leave-related emails/notifications.

                    Mandatory rules:
                    1. Use the supplied Leave MCP tools for every Leave business operation.
                    2. Never invent leave balances, requests, dates, eligibility results, durations,
                       holidays, weekends or calendar information.
                    3. Do not access a database, repository or SQL directly.
                    4. Do not implement PDF, DOCX, email or notification generation inside this agent.
                    5. For a document request, first obtain/verify the required leave information with
                       Leave MCP, then delegate document creation to Document Agent through A2A.
                    6. For an email/notification request, first obtain/verify the required leave
                       information with Leave MCP, then delegate sending to Notification Agent through A2A.
                    7. Never claim that a document was created or a notification was sent unless the
                       corresponding A2A call returns a result.
                    8. If MCP or A2A fails, clearly report the failure and never fabricate success.
                    9. Respect the authenticated user's authorization and do not expose another employee's
                       information without authorization.
                    10. Keep responses concise and user-friendly. Do not expose internal protocol details,
                        credentials, tokens, database details or SQL.
                    """)
        .model(geminiModel)
        .tools(
            ImmutableList.of(
                leaveMcpToolset,
                FunctionTool.create(documentAgentClient, "createLeaveDocument"),
                FunctionTool.create(notificationAgentClient, "sendLeaveEmail")))
        .build();
  }

  public BaseAgent agent() {
    return build();
  }

  public LeaveAiProperties properties() {
    return properties;
  }
}
