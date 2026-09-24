package com.shrija.leave.a2a;

import com.shrija.leave.config.LeaveAiProperties;
import com.shrija.leave.service.AuthorizationService;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NotificationAgentClient {

  private final LeaveAiProperties properties;
  private final A2AAgentClientSupport support;
  private final AuthorizationService authorizationService;

  public NotificationAgentClient(
      LeaveAiProperties properties,
      A2AAgentClientSupport support,
      AuthorizationService authorizationService) {
    this.properties = properties;
    this.support = support;
    this.authorizationService = authorizationService;
  }

  /**
   * Thin FunctionTool bridge. The actual operation is performed by the dedicated A2A agent. The
   * actual email/notification is performed by Notification Agent over A2A.
   */
  public Map<String, Object> sendLeaveEmail(
      String requesterEmployeeId,
      String requesterRole,
      String employeeId,
      String recipient,
      String subject,
      String body) {

    authorizationService.requireSelfOrPrivileged(requesterEmployeeId, requesterRole, employeeId);

    String response =
        support.call(
            properties.notificationAgentUrl(),
            "Send a leave-related email for employee "
                + employeeId
                + ". Requested by employee "
                + requesterEmployeeId
                + " with role "
                + requesterRole
                + ". Recipient: "
                + recipient
                + ". Subject: "
                + subject
                + ". Body: "
                + body
                + ". Send only this notification; do not modify leave records.");

    return Map.of(
        "sent", true,
        "employeeId", employeeId,
        "notificationAgentResponse", response);
  }
}
