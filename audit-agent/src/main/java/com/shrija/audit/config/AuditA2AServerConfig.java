package com.shrija.audit.config;

import com.google.adk.a2a.executor.AgentExecutorConfig;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.sessions.InMemorySessionService;
import com.shrija.audit.agent.AuditAgent;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditA2AServerConfig {

  @Bean
  public AgentCard agentCard(@Value("${server.port:8096}") int port) {
    AgentSkill skill =
        new AgentSkill.Builder()
            .id("audit-events")
            .name("Audit Events")
            .description(
                "Record, search and retrieve confirmed HRMS audit events, including employee, agent "
                    + "and sensitive-operation history.")
            .tags(List.of("audit", "audit-events", "history", "sensitive-operations", "hrms"))
            .build();

    return new AgentCard.Builder()
        .name("audit-agent")
        .description(
            "Audit Agent for audit events, history, searches and sensitive-operation records.")
        .url("http://localhost:" + port)
        .version("1.0.0")
        .protocolVersion("0.3.0")
        .capabilities(new AgentCapabilities.Builder().streaming(false).build())
        .defaultInputModes(List.of("text"))
        .defaultOutputModes(List.of("text"))
        .skills(List.of(skill))
        .build();
  }

  @Bean
  public io.a2a.server.agentexecution.AgentExecutor agentExecutor(AuditAgent auditAgent) {
    return new com.google.adk.a2a.executor.AgentExecutor.Builder()
        .agent(auditAgent.build())
        .appName("audit-agent")
        .sessionService(new InMemorySessionService())
        .artifactService(new InMemoryArtifactService())
        .agentExecutorConfig(AgentExecutorConfig.builder().build())
        .build();
  }
}
