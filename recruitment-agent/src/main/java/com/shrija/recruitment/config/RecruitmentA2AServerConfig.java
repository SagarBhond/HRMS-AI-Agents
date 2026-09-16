package com.shrija.recruitment.config;

import com.google.adk.a2a.executor.AgentExecutorConfig;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.sessions.InMemorySessionService;
import com.shrija.recruitment.agent.RecruitmentAgent;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecruitmentA2AServerConfig {

  @Bean
  public AgentCard agentCard(@Value("${server.port:8095}") int port) {
    AgentSkill recruitmentSkill =
        new AgentSkill.Builder()
            .id("recruitment-operations")
            .name("Recruitment Jobs, Candidates, Interviews & Offers")
            .description(
                "Create and manage job openings, candidates, recruitment stages, interviews and offers. "
                    + "All recruitment data access goes through the shared MCP server.")
            .tags(
                List.of(
                    "recruitment",
                    "jobs",
                    "candidates",
                    "screening",
                    "interviews",
                    "offers",
                    "hrms"))
            .build();

    return new AgentCard.Builder()
        .name("recruitment-agent")
        .description(
            "Recruitment Agent for job openings, candidates, screening, interviews, pipeline stages and offers.")
        .url("http://localhost:" + port)
        .version("1.0.0")
        .protocolVersion("0.3.0")
        .capabilities(new AgentCapabilities.Builder().streaming(false).build())
        .defaultInputModes(List.of("text"))
        .defaultOutputModes(List.of("text"))
        .skills(List.of(recruitmentSkill))
        .build();
  }

  @Bean
  public io.a2a.server.agentexecution.AgentExecutor agentExecutor(RecruitmentAgent recruitmentAgent) {
    return new com.google.adk.a2a.executor.AgentExecutor.Builder()
        .agent(recruitmentAgent.build())
        .appName("recruitment-agent")
        .sessionService(new InMemorySessionService())
        .artifactService(new InMemoryArtifactService())
        .agentExecutorConfig(AgentExecutorConfig.builder().build())
        .build();
  }
}
