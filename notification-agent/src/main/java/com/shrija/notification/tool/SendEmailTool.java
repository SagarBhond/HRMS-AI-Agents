package com.shrija.notification.tool;

import com.google.common.collect.ImmutableList;
import java.util.List;

/** Groups the email capability owned by Notification Agent. */
public final class SendEmailTool {
  private SendEmailTool() {}

  public static final String SEND_EMAIL = "sendEmail";

  public static List<String> toolNames() {
    return ImmutableList.of(SEND_EMAIL);
  }
}
