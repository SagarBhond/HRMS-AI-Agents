package com.shrija.manager.a2a;

import com.shrija.manager.config.ManagerAiProperties;
import org.springframework.stereotype.Component;

@Component
public class PerformanceAgentClient {
  private final ManagerAiProperties properties;
  private final A2AAgentClientSupport support;

  public PerformanceAgentClient(ManagerAiProperties properties, A2AAgentClientSupport support) {
    this.properties = properties;
    this.support = support;
  }

  public String getTeamPerformanceSummary(String managerEmployeeId) {
    return support.call(
        properties.performanceAgentUrl(),
        "Manager "
            + managerEmployeeId
            + " requests confirmed team performance information. Return only confirmed data.");
  }
}
