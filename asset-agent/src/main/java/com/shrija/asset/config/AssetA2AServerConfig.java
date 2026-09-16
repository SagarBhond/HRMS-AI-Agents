package com.shrija.asset.config;

import com.google.adk.a2a.executor.AgentExecutorConfig;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.sessions.InMemorySessionService;
import com.shrija.asset.agent.AssetAgent;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AssetA2AServerConfig {

  @Bean
  public AgentCard agentCard(@Value("${server.port:8093}") int port) {
    AgentSkill skill =
        new AgentSkill.Builder()
            .id("asset-management")
            .name("Asset Management")
            .description(
                "Assign, return, track, search and report HRMS equipment and other employee assets.")
            .tags(List.of("asset", "inventory", "assignment", "equipment"))
            .build();

    return new AgentCard.Builder()
        .name("asset-agent")
        .description(
            "Asset Agent for HRMS asset assignment, returns, inventory, history and reporting.")
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
  public io.a2a.server.agentexecution.AgentExecutor agentExecutor(AssetAgent agent) {
    return new com.google.adk.a2a.executor.AgentExecutor.Builder()
        .agent(agent.build())
        .appName("asset-agent")
        .sessionService(new InMemorySessionService())
        .artifactService(new InMemoryArtifactService())
        .agentExecutorConfig(AgentExecutorConfig.builder().build())
        .build();
  }
}
