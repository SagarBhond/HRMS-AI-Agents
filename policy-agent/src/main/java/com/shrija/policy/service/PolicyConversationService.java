package com.shrija.policy.service;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.common.collect.ImmutableList;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.shrija.policy.agent.PolicyAgent;
import com.shrija.policy.dto.PolicyChatRequest;
import com.shrija.policy.exception.PolicyAgentExecutionException;
import io.reactivex.rxjava3.core.Flowable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PolicyConversationService {
  private static final Logger log = LoggerFactory.getLogger(PolicyConversationService.class);
  private final InMemoryRunner runner;
  private final String appName;

  public PolicyConversationService(PolicyAgent agent) {
    BaseAgent built = agent.build();
    this.appName = built.name();
    this.runner = new InMemoryRunner(built);
  }

  public Result converse(PolicyChatRequest request) {
    String sessionId =
        request.sessionId() == null || request.sessionId().isBlank()
            ? UUID.randomUUID().toString()
            : request.sessionId();
    try {
      ensureSession(request.userId(), sessionId);
      String contextualMessage =
          "Authenticated actor: "
              + request.userId()
              + "; role: "
              + request.role()
              + ".\nUser request: "
              + request.message();
      Content message =
          Content.builder()
              .role("user")
              .parts(ImmutableList.of(Part.builder().text(contextualMessage).build()))
              .build();
      Flowable<Event> events =
          runner.runAsync(request.userId(), sessionId, message, RunConfig.builder().build());
      return new Result(sessionId, collect(events));
    } catch (Exception ex) {
      log.error("Policy Agent failed for actor={} session={}", request.userId(), sessionId, ex);
      throw new PolicyAgentExecutionException(
          "Policy Agent could not process the request right now.", ex);
    }
  }

  private void ensureSession(String userId, String sessionId) {
    Session existing =
        runner
            .sessionService()
            .getSession(appName, userId, sessionId, Optional.empty())
            .blockingGet();
    if (existing == null)
      runner.sessionService().createSession(appName, userId, null, sessionId).blockingGet();
  }

  private String collect(Flowable<Event> events) {
    List<Event> collected = events.toList().blockingGet();
    StringBuilder result = new StringBuilder();
    for (Event event : collected) result.append(event.stringifyContent());
    return result.toString().stripTrailing();
  }

  public record Result(String sessionId, String responseText) {}
}
