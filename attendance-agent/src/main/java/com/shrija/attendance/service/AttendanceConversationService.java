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

import com.shrija.attendance.exception.AttendanceAgentExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AttendanceConversationService {

  private static final Logger log = LoggerFactory.getLogger(AttendanceConversationService.class);
  private final InMemoryRunner runner;

  public AttendanceConversationService(AttendanceAgent attendanceAgent) {
    this.runner = new InMemoryRunner(attendanceAgent.agent());
  }

  public ConversationResult converse(AttendanceChatRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("Attendance chat request must not be null.");
    }

    try {
      String effectiveUserId =
              request.userId() == null || request.userId().isBlank()
                      ? "anonymous"
                      : request.userId();

      String effectiveSessionId =
              request.sessionId() == null || request.sessionId().isBlank()
                      ? UUID.randomUUID().toString()
                      : request.sessionId();

      ensureSession(effectiveUserId, effectiveSessionId);

      String contextualMessage =
              buildContextualMessage(request, effectiveUserId);

      Content userMessage =
              Content.builder()
                      .role("user")
                      .parts(
                              ImmutableList.of(
                                      Part.builder()
                                              .text(contextualMessage)
                                              .build()))
                      .build();

      List<Event> events =
              runner
                      .runAsync(
                              effectiveUserId,
                              effectiveSessionId,
                              userMessage,
                              RunConfig.builder().build())
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

      return new ConversationResult(
              effectiveSessionId,
              response.toString().stripTrailing());

    } catch (Exception ex) {
      // Restore the interrupt flag before anything else so the container can react to shutdown.
      if (containsCause(ex, InterruptedException.class)) {
        Thread.currentThread().interrupt();
      }
      log.error(
          "Attendance Agent failed for actor={} session={}", request.userId(), request.sessionId(), ex);

      // Failures that already carry a precise meaning (authorization, bad input, an unreachable
      // A2A peer or MCP dependency) keep it, so the advice can map them to the right status
      // (403 / 400 / 503) instead of every failure collapsing into a generic 500.
      rethrowIfMeaningful(ex);

      throw new AttendanceAgentExecutionException(
              "Failed to execute attendance agent.",
              ex);
    }
  }

  /**
   * RxJava / the ADK runner wrap the real failure inside a {@code RuntimeException}, so an
   * authorization failure or an unreachable A2A/MCP dependency raised deep in a tool call would
   * otherwise be indistinguishable from a generic model error. Walk the cause chain and re-throw
   * the meaningful type unchanged.
   */
  private void rethrowIfMeaningful(Throwable ex) {
    SecurityException security = findCause(ex, SecurityException.class);
    if (security != null) {
      throw security;
    }
    IllegalStateException unavailable = findCause(ex, IllegalStateException.class);
    if (unavailable != null) {
      throw unavailable;
    }
    IllegalArgumentException badInput = findCause(ex, IllegalArgumentException.class);
    if (badInput != null) {
      throw badInput;
    }
  }

  private static <T extends Throwable> T findCause(Throwable ex, Class<T> type) {
    Throwable current = ex;
    int guard = 0;
    while (current != null && guard++ < 20) {
      if (type.isInstance(current)) {
        return type.cast(current);
      }
      if (current.getCause() == current) {
        break;
      }
      current = current.getCause();
    }
    return null;
  }

  private static boolean containsCause(Throwable ex, Class<? extends Throwable> type) {
    return findCause(ex, type) != null;
  }

  /**
   * Grounds the requester's identity, role, and (optionally) the target employee id in trusted
   * request context rather than leaving the LLM to infer or fabricate them from free text. Mirrors
   * the pattern already used by EmployeeConversationService.
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
    context
        .append("; requestedDate: ")
        .append(request.date() == null ? "not specified" : request.date());

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
