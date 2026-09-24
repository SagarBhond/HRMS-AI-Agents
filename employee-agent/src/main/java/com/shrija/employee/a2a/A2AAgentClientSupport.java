package com.shrija.employee.a2a;

import com.google.adk.a2a.agent.RemoteA2AAgent;
import com.google.adk.agents.BaseAgent;
import com.shrija.employee.exception.A2AAgentUnavailableException;
import io.a2a.client.Client;
import io.a2a.client.config.ClientConfig;
import io.a2a.client.http.A2ACardResolver;
import io.a2a.client.http.JdkA2AHttpClient;
import io.a2a.client.transport.jsonrpc.JSONRPCTransport;
import io.a2a.client.transport.jsonrpc.JSONRPCTransportConfig;
import io.a2a.spec.AgentCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class A2AAgentClientSupport {

  private static final Logger log = LoggerFactory.getLogger(A2AAgentClientSupport.class);

  public BaseAgent connect(String baseUrl, String fallbackName, String fallbackDescription) {
    if (baseUrl == null || baseUrl.isBlank()) {
      throw new IllegalArgumentException(
          "A2A target URL is not configured for " + safeName(fallbackName));
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
      if (card == null) {
        throw new A2AAgentUnavailableException(
            safeName(fallbackName), baseUrl, new IllegalStateException("Agent card was null"));
      }
      Client client =
          Client.builder(card)
              .withTransport(JSONRPCTransport.class, new JSONRPCTransportConfig())
              .clientConfig(
                  new ClientConfig.Builder().setStreaming(card.capabilities().streaming()).build())
              .build();
      return RemoteA2AAgent.builder()
          .name(isBlank(card.name()) ? fallbackName : card.name())
          .description(isBlank(card.description()) ? fallbackDescription : card.description())
          .a2aClient(client)
          .agentCard(card)
          .build();
    } catch (A2AAgentUnavailableException ex) {
      throw ex;
    } catch (RuntimeException ex) {
      // Log the full target (internal topology); the exception message stays URL-free so the
      // advice can return it without leaking host/port details to the caller.
      log.error(
          "A2A connect failed agent={} baseUrl={} cardUrl={}",
          safeName(fallbackName),
          baseUrl,
          agentCardUrl,
          ex);
      throw new A2AAgentUnavailableException(safeName(fallbackName), baseUrl, ex);
    }
  }

  /**
   * Null-safe guard: {@code card.name()} / {@code card.description()} may be null as well as blank,
   * which previously threw a NullPointerException inside the connect path.
   */
  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private static String safeName(String fallbackName) {
    return isBlank(fallbackName) ? "A2A agent" : fallbackName;
  }
}
