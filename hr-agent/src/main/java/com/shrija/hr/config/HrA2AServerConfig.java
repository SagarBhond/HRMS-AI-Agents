package com.shrija.hr.config;

import com.google.adk.a2a.executor.AgentExecutorConfig;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.sessions.InMemorySessionService;
import com.shrija.hr.agent.HrAgent;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes hr-agent as an A2A server, mirroring AttendanceA2AServerConfig. Without this, hr-agent
 * could only ever be an A2A *client* and could never be discovered or invoked over A2A by
 * Employee, Leave, or Payroll.
 */
@Configuration
public class HrA2AServerConfig {

  @Bean
  public AgentCard agentCard(@Value("${server.port:8088}") int port) {
    AgentSkill hrOps =
        new AgentSkill.Builder()
            .id("hr-operations")
            .name("Employee Lifecycle, Policies & Recruitment")
            .description(
                "Handles employee onboarding, promotions, transfers, exits, lifecycle history, "
                    + "and employee record management through the HR and Employee MCP tools, and "
                    + "coordinates with Payroll and Budget agents.")
            .tags(List.of("hr", "hrms", "lifecycle", "onboarding", "promotion", "exit"))
            .build();

    return new AgentCard.Builder()
        .name("hr-agent")
        .description(
            "Handles HRMS employee lifecycle, policies, and recruitment through MCP, and "
                + "coordinates with Payroll and Budget agents through A2A.")
        .url("http://localhost:" + port)
        .version("1.0.0")
        .protocolVersion("0.3.0")
        .capabilities(new AgentCapabilities.Builder().streaming(false).build())
        .defaultInputModes(List.of("text"))
        .defaultOutputModes(List.of("text"))
        .skills(List.of(hrOps))
        .build();
  }

  @Bean
  public io.a2a.server.agentexecution.AgentExecutor agentExecutor(HrAgent hrAgent) {
    return new com.google.adk.a2a.executor.AgentExecutor.Builder()
        .agent(hrAgent.agent())
        .appName("hr-agent")
        .sessionService(new InMemorySessionService())
        .artifactService(new InMemoryArtifactService())
        .agentExecutorConfig(AgentExecutorConfig.builder().build())
        .build();
  }
}
