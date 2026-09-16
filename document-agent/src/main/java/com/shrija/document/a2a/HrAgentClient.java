package com.shrija.document.a2a;

import com.google.adk.agents.BaseAgent;
import com.shrija.document.config.DocumentAiProperties;
import org.springframework.stereotype.Component;

@Component
public class HrAgentClient {
  private final DocumentAiProperties properties;
  private final A2AAgentClientSupport support;
  public HrAgentClient(DocumentAiProperties properties, A2AAgentClientSupport support) {
    this.properties = properties; this.support = support;
  }
  public BaseAgent connect() {
    return support.connect(properties.hrAgentUrl(), "hr-agent", "HR Agent");
  }
}
