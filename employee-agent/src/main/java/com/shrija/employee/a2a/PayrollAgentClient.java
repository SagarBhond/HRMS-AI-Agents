package com.shrija.employee.a2a;

import com.google.adk.agents.BaseAgent;
import com.shrija.employee.config.EmployeeAiProperties;
import org.springframework.stereotype.Component;

@Component
public class PayrollAgentClient {
  private final EmployeeAiProperties properties;
  private final A2AAgentClientSupport support;

  public PayrollAgentClient(EmployeeAiProperties properties, A2AAgentClientSupport support) {
    this.properties = properties;
    this.support = support;
  }

  public BaseAgent connect() {
    return support.connect(properties.payrollAgentUrl(), "payroll-agent", "Payroll Agent");
  }
}
