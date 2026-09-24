package com.shrija.employee.service;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.common.collect.ImmutableList;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.shrija.employee.agent.EmployeeAgent;
import com.shrija.employee.dto.EmployeeChatRequest;
import com.shrija.employee.exception.EmployeeAgentExecutionException;
import io.reactivex.rxjava3.core.Flowable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmployeeConversationService {

  private static final Logger log = LoggerFactory.getLogger(EmployeeConversationService.class);
  private final InMemoryRunner runner;
  private final String appName;

  public EmployeeConversationService(EmployeeAgent employeeAgent) {
    BaseAgent agent = employeeAgent.build();
    this.appName = agent.name();
    this.runner = new InMemoryRunner(agent);
  }

  public Result converse(EmployeeChatRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("Employee chat request must not be null.");
    }
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
      // Restore the interrupt flag before anything else so the container can react to shutdown.
      if (containsCause(ex, InterruptedException.class)) {
        Thread.currentThread().interrupt();
      }
      log.error("Employee Agent failed for actor={} session={}", request.userId(), sessionId, ex);

      // Failures that already carry a precise meaning keep it, so the advice can map them to the
      // right status (403 / 400 / 503) instead of every failure collapsing into one bucket.
      rethrowIfMeaningful(ex);

      throw new EmployeeAgentExecutionException(
          "Employee Agent could not process the request right now.", ex);
    }
  }

  private void ensureSession(String userId, String sessionId) {
    Session existing =
        runner
            .sessionService()
            .getSession(appName, userId, sessionId, Optional.empty())
            .blockingGet();
    if (existing == null) {
      runner.sessionService().createSession(appName, userId, null, sessionId).blockingGet();
    }
  }

  private String collect(Flowable<Event> events) {
    List<Event> collected = events.toList().blockingGet();
    StringBuilder result = new StringBuilder();
    if (collected != null) {
      for (Event event : collected) {
        if (event == null) {
          continue;
        }
        String content = event.stringifyContent();
        if (content != null) {
          result.append(content);
        }
      }
    }
    return result.toString().stripTrailing();
  }

  /**
   * RxJava wraps the real failure inside a {@code RuntimeException}, so an authorization or
   * dependency failure raised deep in a tool call would otherwise be indistinguishable from a model
   * error. Walk the cause chain and re-throw the meaningful type unchanged.
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

  public record Result(String sessionId, String responseText) {}
}
