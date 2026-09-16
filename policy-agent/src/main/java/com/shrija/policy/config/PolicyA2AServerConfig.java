package com.shrija.policy.config;

import com.google.adk.a2a.executor.AgentExecutorConfig;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.sessions.InMemorySessionService;
import com.shrija.policy.agent.PolicyAgent;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PolicyA2AServerConfig {

  @Bean
  public AgentCard agentCard(@Value("${server.port:8091}") int port) {
    AgentSkill skill =
        new AgentSkill.Builder()
            .id("policy-retrieval")
            .name("Published Policy Retrieval")
            .description(
                "Retrieve and explain published Leave, Attendance, WFH, Travel, Expense, "
                    + "Code of Conduct, Notice Period and Promotion policies from MCP.")
            .tags(List.of("policy", "hrms", "policy-retrieval"))
            .build();

    return new AgentCard.Builder()
        .name("policy-agent")
        .description(
            "Policy Agent that retrieves published HRMS policy information from the "
                + "Policy MCP source of truth.")
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
  public io.a2a.server.agentexecution.AgentExecutor agentExecutor(PolicyAgent agent) {
    return new com.google.adk.a2a.executor.AgentExecutor.Builder()
        .agent(agent.build())
        .appName("policy-agent")
        .sessionService(new InMemorySessionService())
        .artifactService(new InMemoryArtifactService())
        .agentExecutorConfig(AgentExecutorConfig.builder().build())
        .build();
  }
}
