package com.shrija.document.a2a;

import com.google.adk.agents.BaseAgent;
import com.shrija.document.config.DocumentAiProperties;
import org.springframework.stereotype.Component;

@Component
public class LeaveAgentClient {
  private final DocumentAiProperties properties;
  private final A2AAgentClientSupport support;
  public LeaveAgentClient(DocumentAiProperties properties, A2AAgentClientSupport support) {
    this.properties = properties; this.support = support;
  }
  public BaseAgent connect() {
    return support.connect(properties.leaveAgentUrl(), "leave-agent", "Leave Agent");
  }
}
