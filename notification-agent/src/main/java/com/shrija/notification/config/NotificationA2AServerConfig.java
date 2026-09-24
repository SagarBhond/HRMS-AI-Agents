package com.shrija.notification.config;

import com.google.adk.a2a.executor.AgentExecutorConfig;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.sessions.InMemorySessionService;
import com.shrija.notification.agent.NotificationAgent;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationA2AServerConfig {

  @Bean
  public AgentCard agentCard(@Value("${server.port:8090}") int port) {
    AgentSkill skill =
        new AgentSkill.Builder()
            .id("notification-delivery")
            .name("Email and HRMS Notification Delivery")
            .description(
                "Send email and employee, manager, HR, payroll, leave and document notifications.")
            .tags(List.of("notification", "email", "hrms", "delivery"))
            .build();

    return new AgentCard.Builder()
        .name("notification-agent")
        .description(
            "Notification Agent for email and HRMS notifications. "
                + "All notification operations use the shared MCP server.")
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
  public io.a2a.server.agentexecution.AgentExecutor agentExecutor(NotificationAgent agent) {
    return new com.google.adk.a2a.executor.AgentExecutor.Builder()
        .agent(agent.build())
        .appName("notification-agent")
        .sessionService(new InMemorySessionService())
        .artifactService(new InMemoryArtifactService())
        .agentExecutorConfig(AgentExecutorConfig.builder().build())
        .build();
  }
}
