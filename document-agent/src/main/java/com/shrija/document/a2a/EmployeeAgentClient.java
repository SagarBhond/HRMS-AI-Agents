package com.shrija.document.a2a;

import com.google.adk.agents.BaseAgent;
import com.shrija.document.config.DocumentAiProperties;
import org.springframework.stereotype.Component;

@Component
public class EmployeeAgentClient {
  private final DocumentAiProperties properties;
  private final A2AAgentClientSupport support;
  public EmployeeAgentClient(DocumentAiProperties properties, A2AAgentClientSupport support) {
    this.properties = properties; this.support = support;
  }
  public BaseAgent connect() {
    return support.connect(properties.employeeAgentUrl(), "employee-agent", "Employee Agent");
  }
}
