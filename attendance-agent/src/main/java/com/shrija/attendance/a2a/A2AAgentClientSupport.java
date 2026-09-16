package com.shrija.attendance.a2a;

import com.google.adk.a2a.agent.RemoteA2AAgent;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.RunConfig;
// import com.google.adk.content.Content;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.common.collect.ImmutableList;
import com.google.genai.types.Part;
import io.a2a.client.Client;
import io.a2a.client.config.ClientConfig;
import io.a2a.client.http.A2ACardResolver;
import io.a2a.client.http.JdkA2AHttpClient;
import io.a2a.client.transport.jsonrpc.JSONRPCTransport;
import io.a2a.client.transport.jsonrpc.JSONRPCTransportConfig;
import io.a2a.spec.AgentCard;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class A2AAgentClientSupport {

  public String call(String baseUrl, String prompt) {
    try {
      String agentCardUrl =
          baseUrl.endsWith("/")
              ? baseUrl + ".well-known/agent-card.json"
              : baseUrl + "/.well-known/agent-card.json";
      AgentCard card =
          new A2ACardResolver(new JdkA2AHttpClient(), baseUrl, agentCardUrl).getAgentCard();

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
      for (Event event : events) {
        response.append(event.stringifyContent());
      }
      return response.toString().stripTrailing();
    } catch (Exception ex) {
      throw new IllegalStateException("A2A agent communication failed: " + ex.getMessage(), ex);
    }
  }
}
