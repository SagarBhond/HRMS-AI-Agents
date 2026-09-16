package com.shrija.employee.a2a;

import com.google.adk.a2a.agent.RemoteA2AAgent;
import com.google.adk.agents.BaseAgent;
import io.a2a.client.Client;
import io.a2a.client.config.ClientConfig;
import io.a2a.client.http.A2ACardResolver;
import io.a2a.client.http.JdkA2AHttpClient;
import io.a2a.client.transport.jsonrpc.JSONRPCTransport;
import io.a2a.client.transport.jsonrpc.JSONRPCTransportConfig;
import io.a2a.spec.AgentCard;
import org.springframework.stereotype.Component;

@Component
public class A2AAgentClientSupport {

  public BaseAgent connect(String baseUrl, String fallbackName, String fallbackDescription) {
    if (baseUrl == null || baseUrl.isBlank()) {
      throw new IllegalArgumentException("A2A target URL is not configured");
    }
    // Spec-standard discovery path (matches attendance-agent's client and the AgentCard.url()
    // each agent publishes itself at). Previously this used "/card", which does not match the
    // well-known discovery convention and will 404 against agents using the standard path.
    String agentCardUrl =
        baseUrl.endsWith("/")
            ? baseUrl + ".well-known/agent-card.json"
            : baseUrl + "/.well-known/agent-card.json";
    try {
      AgentCard card =
          new A2ACardResolver(new JdkA2AHttpClient(), baseUrl, agentCardUrl).getAgentCard();
      Client client =
          Client.builder(card)
              .withTransport(JSONRPCTransport.class, new JSONRPCTransportConfig())
              .clientConfig(
                  new ClientConfig.Builder().setStreaming(card.capabilities().streaming()).build())
              .build();
      return RemoteA2AAgent.builder()
          .name(card.name().isBlank() ? fallbackName : card.name())
          .description(card.description().isBlank() ? fallbackDescription : card.description())
          .a2aClient(client)
          .agentCard(card)
          .build();
    } catch (RuntimeException ex) {
      throw new IllegalStateException("A2A agent is unavailable at " + baseUrl, ex);
    }
  }
}
