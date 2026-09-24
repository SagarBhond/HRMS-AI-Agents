package com.shrija.notification.tool;

import com.google.common.collect.ImmutableList;
import java.util.List;

/** Groups recipient-specific notification capabilities owned by Notification Agent. */
public final class SendNotificationTool {
  private SendNotificationTool() {}

  public static final String SEND_EMPLOYEE_NOTIFICATION = "sendEmployeeNotification";
  public static final String SEND_MANAGER_NOTIFICATION = "sendManagerNotification";
  public static final String SEND_HR_NOTIFICATION = "sendHrNotification";
  public static final String SEND_PAYROLL_NOTIFICATION = "sendPayrollNotification";
  public static final String SEND_LEAVE_NOTIFICATION = "sendLeaveNotification";
  public static final String SEND_DOCUMENT_NOTIFICATION = "sendDocumentNotification";

  public static List<String> toolNames() {
    return ImmutableList.of(
        SEND_EMPLOYEE_NOTIFICATION,
        SEND_MANAGER_NOTIFICATION,
        SEND_HR_NOTIFICATION,
        SEND_PAYROLL_NOTIFICATION,
        SEND_LEAVE_NOTIFICATION,
        SEND_DOCUMENT_NOTIFICATION);
  }
}
