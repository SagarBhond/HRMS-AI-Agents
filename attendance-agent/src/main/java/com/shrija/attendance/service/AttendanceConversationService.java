package com.shrija.attendance.service;

import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.common.collect.ImmutableList;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.shrija.attendance.agent.AttendanceAgent;
import com.shrija.attendance.dto.AttendanceChatRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AttendanceConversationService {

  private final InMemoryRunner runner;

  public AttendanceConversationService(AttendanceAgent attendanceAgent) {
    this.runner = new InMemoryRunner(attendanceAgent.agent());
  }

  public ConversationResult converse(AttendanceChatRequest request) {
    String effectiveUserId =
            request.userId() == null || request.userId().isBlank() ? "anonymous" : request.userId();
    String effectiveSessionId =
            request.sessionId() == null || request.sessionId().isBlank()
                    ? UUID.randomUUID().toString()
                    : request.sessionId();

    ensureSession(effectiveUserId, effectiveSessionId);

    String contextualMessage = buildContextualMessage(request, effectiveUserId);

    Content userMessage =
            Content.builder()
                    .role("user")
                    .parts(ImmutableList.of(Part.builder().text(contextualMessage).build()))
                    .build();

    List<Event> events =
            runner
                    .runAsync(effectiveUserId, effectiveSessionId, userMessage, RunConfig.builder().build())
                    .toList()
                    .blockingGet();

    StringBuilder response = new StringBuilder();
    for (Event event : events) {
      response.append(event.stringifyContent());
    }
    return new ConversationResult(effectiveSessionId, response.toString().stripTrailing());
  }

  /**
   * Grounds the requester's identity, role, and (optionally) the target employee id
   * in trusted request context rather than leaving the LLM to infer or fabricate
   * them from free text. Mirrors the pattern already used by EmployeeConversationService.
   */
  private String buildContextualMessage(AttendanceChatRequest request, String requesterEmployeeId) {
    StringBuilder context = new StringBuilder();
    context
            .append("Authenticated actor: ")
            .append(requesterEmployeeId)
            .append("; role: ")
            .append(request.role())
            .append("; requesterEmployeeId: ")
            .append(requesterEmployeeId);

    String targetEmployeeId =
            request.employeeId() != null && !request.employeeId().isBlank()
                    ? request.employeeId()
                    : requesterEmployeeId;
    context.append("; targetEmployeeId: ").append(targetEmployeeId);

    // Ground "today" the same way targetEmployeeId is grounded above — always emit
    // a literal date so the model has nothing to guess or fabricate.
    String effectiveDate =
            request.date() != null && !request.date().isBlank()
                    ? request.date()
                    : java.time.LocalDate.now().toString();
    context.append("; currentDate: ").append(effectiveDate);
    context.append("; requestedDate: ").append(request.date() == null ? "not specified" : request.date());

    context.append(".\nUser request: ").append(request.message());
    return context.toString();
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
