package com.shrija.payroll.a2a;

import com.google.adk.agents.BaseAgent;
import com.shrija.payroll.config.PayrollAiProperties;
import org.springframework.stereotype.Component;

@Component
public class AttendanceAgentClient {
  private final PayrollAiProperties properties;
  private final A2AAgentClientSupport support;

  public AttendanceAgentClient(PayrollAiProperties properties, A2AAgentClientSupport support) {
    this.properties = properties;
    this.support = support;
  }

  public BaseAgent connect() {
    return support.connect(properties.attendanceAgentUrl(), "attendance-agent", "Attendance Agent");
  }
}
