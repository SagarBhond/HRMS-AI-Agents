package com.shrija.notification.agent;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.Gemini;
import com.google.adk.tools.mcp.McpToolset;
import com.google.common.collect.ImmutableList;
import com.shrija.notification.config.NotificationAiProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class NotificationAgent {

  private final Gemini geminiModel;
  private final McpToolset notificationMcpToolset;
  private final NotificationAiProperties properties;

  public NotificationAgent(
      Gemini geminiModel,
      @Qualifier("notificationMcpToolset") McpToolset notificationMcpToolset,
      NotificationAiProperties properties) {
    this.geminiModel = geminiModel;
    this.notificationMcpToolset = notificationMcpToolset;
    this.properties = properties;
  }

  public BaseAgent build() {
    return LlmAgent.builder()
        .name("notification-agent")
        .description(
            "Notification Agent for HRMS email and employee, manager, HR, payroll, leave and "
                + "document notifications. All notification operations use MCP.")
        .instruction("""
            You are the Notification Agent for the Shrija HRMS.

            Scope:
            - sendEmail
            - sendEmployeeNotification
            - sendManagerNotification
            - sendHrNotification
            - sendPayrollNotification
            - sendLeaveNotification
            - sendDocumentNotification

            Mandatory rules:
            1. Use only the supplied Notification MCP toolset for notification operations.
            2. Never access a database, repository, SQL or email provider directly.
            3. Never invent recipients, email addresses, notification status or message content.
            4. Never claim a notification was sent unless the MCP tool confirms success.
            5. If MCP fails, clearly report the failure and never fabricate success.
            6. Other domain agents such as Leave, Payroll and HR should call this Notification
               Agent through A2A when they need an email or notification delivered.
            7. Do not call domain agents merely to send a notification; the requesting agent
               should provide the required business context through A2A.
            8. Never expose credentials, tokens, SQL, raw protocol payloads or internal URLs.
            9. Respect the authenticated actor identity and role supplied in the request context.
            10. Keep responses concise and business-friendly.
            """)
        .model(geminiModel)
        .tools(ImmutableList.of(notificationMcpToolset))
        .build();
  }

  public BaseAgent agent() {
    return build();
  }

  public NotificationAiProperties properties() {
    return properties;
  }
}
