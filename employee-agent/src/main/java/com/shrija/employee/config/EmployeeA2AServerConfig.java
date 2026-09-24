package com.shrija.employee.config;

import com.google.adk.a2a.executor.AgentExecutorConfig;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.sessions.InMemorySessionService;
import com.shrija.employee.agent.EmployeeAgent;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmployeeA2AServerConfig {

  @Bean
  public AgentCard agentCard(@Value("${server.port:8083}") int port) {
    AgentSkill employeeLookup =
        new AgentSkill.Builder()
            .id("employee-lookup")
            .name("Employee Lookup & Profile")
            .description(
                "Look up employee profile, contact, department, designation, employment status "
                    + "and reporting-manager information by ID, code or email.")
            .tags(List.of("employee", "profile", "hrms"))
            .build();

    return new AgentCard.Builder()
        .name("employee-agent")
        .description(
            "Primary employee-information agent. Handles employee identity, profile, contact, "
                + "department, designation, manager and employment status through MCP only.")
        .url("http://localhost:" + port)
        .version("1.0.0")
        .protocolVersion("0.3.0")
        .capabilities(new AgentCapabilities.Builder().streaming(false).build())
        .defaultInputModes(List.of("text"))
        .defaultOutputModes(List.of("text"))
        .skills(List.of(employeeLookup))
        .build();
  }

  // Bridges the A2A protocol layer straight into your existing ADK EmployeeAgent —
  // no Spring AI ChatClient involved, this is the google-adk-a2a executor you already
  // depend on via the google-adk-a2a artifact.
  @Bean
  public io.a2a.server.agentexecution.AgentExecutor agentExecutor(EmployeeAgent employeeAgent) {
    return new com.google.adk.a2a.executor.AgentExecutor.Builder()
        .agent(employeeAgent.build())
        .appName("employee-agent")
        .sessionService(new InMemorySessionService())
        .artifactService(new InMemoryArtifactService())
        .agentExecutorConfig(AgentExecutorConfig.builder().build())
        .build();
  }
}
