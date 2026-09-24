package com.hrms.mcpserver.tools;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class NotificationTools {
  private final AtomicLong ids = new AtomicLong(1);
  private final List<NotificationRecord> sent = new CopyOnWriteArrayList<>();

  @Tool(description = "Send an email")
  public NotificationRecord sendEmail(String recipient, String subject, String message) {
    return send("EMAIL", recipient, subject, message);
  }

  @Tool(description = "Send an employee notification")
  public NotificationRecord sendEmployeeNotification(
      Long employeeId, String subject, String message) {
    return send("EMPLOYEE", String.valueOf(employeeId), subject, message);
  }

  @Tool(description = "Send a manager notification")
  public NotificationRecord sendManagerNotification(
      Long managerEmployeeId, String subject, String message) {
    return send("MANAGER", String.valueOf(managerEmployeeId), subject, message);
  }

  @Tool(description = "Send an HR notification")
  public NotificationRecord sendHrNotification(String subject, String message) {
    return send("HR", "HR", subject, message);
  }

  @Tool(description = "Send a payroll notification")
  public NotificationRecord sendPayrollNotification(String subject, String message) {
    return send("PAYROLL", "PAYROLL", subject, message);
  }

  @Tool(description = "Send a leave notification")
  public NotificationRecord sendLeaveNotification(String subject, String message) {
    return send("LEAVE", "LEAVE", subject, message);
  }

  @Tool(description = "Send a document notification")
  public NotificationRecord sendDocumentNotification(String subject, String message) {
    return send("DOCUMENT", "DOCUMENT", subject, message);
  }

  @Tool(description = "List notification records")
  public List<NotificationRecord> listNotifications() {
    return List.copyOf(sent);
  }

  private NotificationRecord send(
      String channel, String recipient, String subject, String message) {
    NotificationRecord r =
        new NotificationRecord(
            ids.getAndIncrement(),
            channel,
            recipient,
            subject,
            message,
            Instant.now().toString(),
            "ACCEPTED");
    sent.add(r);
    return r;
  }

  public record NotificationRecord(
      Long notificationId,
      String channel,
      String recipient,
      String subject,
      String message,
      String createdAt,
      String status) {}
}
