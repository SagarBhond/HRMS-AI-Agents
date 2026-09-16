package com.shrija.employee.a2a;

import com.google.adk.agents.BaseAgent;
import com.shrija.employee.config.EmployeeAiProperties;
import org.springframework.stereotype.Component;

@Component
public class LeaveAgentClient {
  private final EmployeeAiProperties properties;
  private final A2AAgentClientSupport support;

  public LeaveAgentClient(EmployeeAiProperties properties, A2AAgentClientSupport support) {
    this.properties = properties;
    this.support = support;
  }

  public BaseAgent connect() {
    return support.connect(properties.leaveAgentUrl(), "leave-agent", "Leave Agent");
  }
}
