package com.shrija.orchestrator.a2a;

import com.shrija.orchestrator.config.OrchestratorAiProperties;
import org.springframework.stereotype.Component;

@Component
public class ManagerAgentClient {

  private final OrchestratorAiProperties properties;
  private final A2AAgentClientSupport support;

  public ManagerAgentClient(OrchestratorAiProperties properties, A2AAgentClientSupport support) {
    this.properties = properties;
    this.support = support;
  }

  /**
   * Sends an already-grounded (Authenticated actor / role / target / date) message to the Manager
   * Agent.
   */
  public String delegate(String groundedMessage) {
    return support.call(properties.managerAgentUrl(), groundedMessage);
  }
}
