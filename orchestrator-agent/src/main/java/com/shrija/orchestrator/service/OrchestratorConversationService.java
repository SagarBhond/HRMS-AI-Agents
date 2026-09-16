package com.shrija.orchestrator.service;

import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.common.collect.ImmutableList;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.shrija.orchestrator.agent.OrchestratorAgent;
import com.shrija.orchestrator.dto.OrchestratorChatRequest;
import com.shrija.orchestrator.exception.OrchestratorAgentExecutionException;
import com.shrija.orchestrator.security.AuthenticatedUser;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrchestratorConversationService {

  private static final Logger log = LoggerFactory.getLogger(OrchestratorConversationService.class);
  private final InMemoryRunner runner;

  public OrchestratorConversationService(OrchestratorAgent orchestratorAgent) {
    this.runner = new InMemoryRunner(orchestratorAgent.agent());
  }

  public ConversationResult converse(AuthenticatedUser user, OrchestratorChatRequest request) {
    String sessionId =
        request.sessionId() == null || request.sessionId().isBlank()
            ? UUID.randomUUID().toString()
            : request.sessionId();

    try {
      ensureSession(user.userId(), sessionId);
      String contextualMessage = buildContextualMessage(user, request.message());

      Content userMessage =
          Content.builder()
              .role("user")
              .parts(ImmutableList.of(Part.builder().text(contextualMessage).build()))
              .build();

      List<Event> events =
          runner
              .runAsync(user.userId(), sessionId, userMessage, RunConfig.builder().build())
              .toList()
              .blockingGet();

      StringBuilder response = new StringBuilder();
      for (Event event : events) {
        response.append(event.stringifyContent());
      }
      return new ConversationResult(sessionId, response.toString().stripTrailing());
    } catch (Exception ex) {
      log.error("Orchestrator Agent failed for actor={} session={}", user.userId(), sessionId, ex);
      throw new OrchestratorAgentExecutionException(
          "Orchestrator Agent could not process the request right now.", ex);
    }
  }

  /**
   * Builds the trusted "Authenticated actor" context line from the JWT-derived AuthenticatedUser --
   * never from anything the client sent in the request body -- so the LLM (and every downstream
   * agent it delegates to) grounds identity in a value nobody can spoof.
   */
  private String buildContextualMessage(AuthenticatedUser user, String message) {
    String employeeCode =
        (user.employeeCode() == null || user.employeeCode().isBlank())
            ? "UNLINKED"
            : user.employeeCode();
    return "Authenticated actor: "
        + employeeCode
        + "; username: "
        + user.username()
        + "; role: "
        + user.role()
        + "; requesterEmployeeId: "
        + employeeCode
        + "; targetEmployeeId: "
        + employeeCode
        + "; currentDate: "
        + LocalDate.now()
        + ".\nUser request: "
        + message;
  }

  private void ensureSession(String userId, String sessionId) {
    Session existing =
        runner
            .sessionService()
            .getSession(runner.appName(), userId, sessionId, Optional.empty())
            .blockingGet();
    if (existing == null) {
      runner
          .sessionService()
          .createSession(runner.appName(), userId, null, sessionId)
          .blockingGet();
    }
  }

  public record ConversationResult(String sessionId, String responseText) {}
}
