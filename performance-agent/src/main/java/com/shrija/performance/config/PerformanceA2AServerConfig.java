package com.shrija.performance.config;
import com.google.adk.a2a.executor.AgentExecutorConfig;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.sessions.InMemorySessionService;
import com.shrija.performance.agent.PerformanceAgent;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class PerformanceA2AServerConfig {
  @Bean public AgentCard agentCard(@Value("${server.port:8094}") int port) {
    AgentSkill skill=new AgentSkill.Builder().id("performance-operations")
      .name("Performance Goals, Reviews & Reports")
      .description("Manage goals, self reviews, manager reviews, performance reviews and summaries through MCP.")
      .tags(List.of("performance","goals","review","self-review","manager-review","reports","hrms")).build();
    return new AgentCard.Builder().name("performance-agent")
      .description("Performance Agent for goals, reviews and performance summaries.")
      .url("http://localhost:"+port).version("1.0.0").protocolVersion("0.3.0")
      .capabilities(new AgentCapabilities.Builder().streaming(false).build())
      .defaultInputModes(List.of("text")).defaultOutputModes(List.of("text")).skills(List.of(skill)).build();
  }
  @Bean public io.a2a.server.agentexecution.AgentExecutor agentExecutor(PerformanceAgent agent) {
    return new com.google.adk.a2a.executor.AgentExecutor.Builder().agent(agent.build())
      .appName("performance-agent").sessionService(new InMemorySessionService())
      .artifactService(new InMemoryArtifactService()).agentExecutorConfig(AgentExecutorConfig.builder().build()).build();
  }
}
