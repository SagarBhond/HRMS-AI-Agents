package com.shrija.payroll.a2a;

import com.google.adk.agents.BaseAgent;
import com.shrija.payroll.config.PayrollAiProperties;
import org.springframework.stereotype.Component;

@Component
public class LeaveAgentClient {
  private final PayrollAiProperties properties;
  private final A2AAgentClientSupport support;

  public LeaveAgentClient(PayrollAiProperties properties, A2AAgentClientSupport support) {
    this.properties = properties;
    this.support = support;
  }

  public BaseAgent connect() {
    return support.connect(properties.leaveAgentUrl(), "leave-agent", "Leave Agent");
  }
}
