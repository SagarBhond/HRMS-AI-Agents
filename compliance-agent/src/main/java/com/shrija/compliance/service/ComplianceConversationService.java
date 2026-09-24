package com.shrija.compliance.service;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.shrija.compliance.agent.ComplianceAgent;
import com.shrija.compliance.dto.ComplianceChatRequest;
import com.shrija.compliance.exception.ComplianceAgentExecutionException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ComplianceConversationService
{
    private final ComplianceAgent agent;

    public ComplianceConversationService(ComplianceAgent agent)
    {
        this.agent = agent;
    }

    public Result converse(ComplianceChatRequest r)
    {
        try
        {
            BaseAgent root = agent.build();
            InMemoryRunner runner = new InMemoryRunner(root);
            String sessionId = r.sessionId() != null && !r.sessionId().isBlank() ? r.sessionId() : UUID.randomUUID().toString();
            String userId = r.userId() != null && !r.userId().isBlank() ? r.userId() : "anonymous";

            Session session = runner.sessionService()
                    .createSession(root.name(), userId, new java.util.HashMap<>(), sessionId)
                    .blockingGet();

            String context = "Actor: " + userId + "; Role: " + (r.role() == null ? "unknown" : r.role()) + ".\nRequest: " + r.message();
            Content content = Content.builder().role("user").parts(java.util.List.of(Part.fromText(context))).build();

            StringBuilder out = new StringBuilder();
            for (Event e : runner.runAsync(userId, session.id(), content, RunConfig.builder().build()).blockingIterable())
            {
                if (e.stringifyContent() != null) out.append(e.stringifyContent());
            }

            return new Result(session.id(), out.toString());
        }
        catch (Exception e)
        {
            throw new ComplianceAgentExecutionException("Compliance agent execution failed", e);
        }
    }

    public record Result(String sessionId, String responseText) {}
}