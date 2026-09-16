package com.shrija.manager.config;

import com.google.adk.a2a.executor.AgentExecutorConfig;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.sessions.InMemorySessionService;
import com.shrija.manager.agent.ManagerAgent;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes manager-agent as an A2A server, mirroring AttendanceA2AServerConfig / LeaveA2AServerConfig.
 * Every other sub-agent (Employee, Attendance, Leave, Payroll, HR) already points a
 * ManagerAgentClient at this URL to notify or coordinate with the People Manager role.
 */
@Configuration
public class ManagerA2AServerConfig {

  @Bean
  public AgentCard agentCard(@Value("${server.port:8086}") int port) {
    AgentSkill teamManagement =
        new AgentSkill.Builder()
            .id("team-management-and-approvals")
            .name("Team Management & Approvals")
            .description(
                "Handles People Manager operations: team roster and profile visibility, leave "
                    + "approval/rejection decisions, team attendance reporting, and HR escalation.")
            .tags(List.of("manager", "team", "approvals", "hrms"))
            .build();

    return new AgentCard.Builder()
        .name("manager-agent")
        .description(
            "Manager Agent - team management and approvals for the People Manager role. Uses "
                + "MCP for read-only Team/Employee data, and coordinates with Leave, Attendance, "
                + "and HR agents through A2A.")
        .url("http://localhost:" + port)
        .version("1.0.0")
        .protocolVersion("0.3.0")
        .capabilities(new AgentCapabilities.Builder().streaming(false).build())
        .defaultInputModes(List.of("text"))
        .defaultOutputModes(List.of("text"))
        .skills(List.of(teamManagement))
        .build();
  }

  @Bean
  public io.a2a.server.agentexecution.AgentExecutor agentExecutor(ManagerAgent managerAgent) {
    return new com.google.adk.a2a.executor.AgentExecutor.Builder()
        .agent(managerAgent.agent())
        .appName("manager-agent")
        .sessionService(new InMemorySessionService())
        .artifactService(new InMemoryArtifactService())
        .agentExecutorConfig(AgentExecutorConfig.builder().build())
        .build();
  }
}
