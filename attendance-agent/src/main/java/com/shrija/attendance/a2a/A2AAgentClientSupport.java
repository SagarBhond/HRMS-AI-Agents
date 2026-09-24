package com.shrija.attendance.a2a;

import com.google.adk.a2a.agent.RemoteA2AAgent;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.RunConfig;
// import com.google.adk.content.Content;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.common.collect.ImmutableList;
import com.google.genai.types.Part;
import com.shrija.attendance.exception.A2AAgentUnavailableException;
import io.a2a.client.Client;
import io.a2a.client.config.ClientConfig;
import io.a2a.client.http.A2ACardResolver;
import io.a2a.client.http.JdkA2AHttpClient;
import io.a2a.client.transport.jsonrpc.JSONRPCTransport;
import io.a2a.client.transport.jsonrpc.JSONRPCTransportConfig;
import io.a2a.spec.AgentCard;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class A2AAgentClientSupport {

  private static final Logger log = LoggerFactory.getLogger(A2AAgentClientSupport.class);

  public String call(String baseUrl, String prompt) {
    if (baseUrl == null || baseUrl.isBlank()) {
      // Same failure mode as before (caller gets an unavailable-agent error), but now reported
      // clearly instead of falling through to a NullPointerException inside agentCardUrl below.
      throw new A2AAgentUnavailableException(
          "A2A agent", baseUrl, new IllegalArgumentException("A2A target URL is not configured"));
    }

    String agentCardUrl =
        baseUrl.endsWith("/")
            ? baseUrl + ".well-known/agent-card.json"
            : baseUrl + "/.well-known/agent-card.json";

    AgentCard card;
    try {
      card = new A2ACardResolver(new JdkA2AHttpClient(), baseUrl, agentCardUrl).getAgentCard();
    } catch (RuntimeException ex) {
      // Discovery failure: the peer agent is unreachable or not serving its agent card.
      log.error("A2A agent card resolution failed baseUrl={} cardUrl={}", baseUrl, agentCardUrl, ex);
      throw new A2AAgentUnavailableException(baseUrl, baseUrl, ex);
    }
    if (card == null) {
      IllegalStateException cause = new IllegalStateException("Agent card was null");
      log.error("A2A agent card resolution returned null baseUrl={}", baseUrl, cause);
      throw new A2AAgentUnavailableException(baseUrl, baseUrl, cause);
    }

    String agentName = isBlank(card.name()) ? baseUrl : card.name();

    try {
      Client client =
          Client.builder(card)
              .withTransport(JSONRPCTransport.class, new JSONRPCTransportConfig())
              .clientConfig(
                  new ClientConfig.Builder().setStreaming(card.capabilities().streaming()).build())
              .build();

      BaseAgent remote =
          RemoteA2AAgent.builder()
              .name(card.name())
              .description(card.description())
              .a2aClient(client)
              .agentCard(card)
              .build();

      InMemoryRunner runner = new InMemoryRunner(remote);
      String userId = "attendance-agent";
      String sessionId = UUID.randomUUID().toString();
      runner
          .sessionService()
          .createSession(runner.appName(), userId, null, sessionId)
          .blockingGet();

      com.google.genai.types.Content message =
          com.google.genai.types.Content.builder()
              .role("user")
              .parts(ImmutableList.of(Part.builder().text(prompt).build()))
              .build();

      List<Event> events =
          runner
              .runAsync(userId, sessionId, message, RunConfig.builder().build())
              .toList()
              .blockingGet();

      StringBuilder response = new StringBuilder();
      if (events != null) {
        for (Event event : events) {
          if (event == null) {
            continue;
          }
          String content = event.stringifyContent();
          if (content != null) {
            response.append(content);
          }
        }
      }
      return response.toString().stripTrailing();
    } catch (RuntimeException ex) {
      // Call failure: the peer was reachable (we got its card) but the remote invocation itself
      // failed. Logged with full context; the client-facing message stays free of internal detail.
      log.error(
          "A2A call failed agent={} baseUrl={}", agentName, baseUrl, ex);
      throw new A2AAgentUnavailableException(agentName, baseUrl, ex);
    }
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
