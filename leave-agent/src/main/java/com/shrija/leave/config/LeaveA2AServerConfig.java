package com.shrija.leave.config;

import com.google.adk.a2a.executor.AgentExecutorConfig;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.sessions.InMemorySessionService;
import com.shrija.leave.agent.LeaveAgent;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LeaveA2AServerConfig {

  @Bean
  public AgentCard agentCard(@Value("${server.port:8087}") int port) {
    AgentSkill leaveOperations =
        new AgentSkill.Builder()
            .id("leave-operations")
            .name("Leave Requests, Approvals & Balances")
            .description(
                "Manage employee leave applications, approvals, balances, requests, "
                    + "cancellations, modifications, eligibility, overlap checks and leave calendars "
                    + "through the Leave MCP tools.")
            .tags(List.of("leave", "hrms", "approval", "balance", "calendar"))
            .build();

    return new AgentCard.Builder()
        .name("leave-agent")
        .description(
            "Primary leave-management agent. Performs leave business operations through MCP "
                + "and coordinates with Employee, Manager, Payroll, HR, Document and Notification "
                + "agents through A2A.")
        .url("http://localhost:" + port)
        .version("1.0.0")
        .protocolVersion("0.3.0")
        .capabilities(new AgentCapabilities.Builder().streaming(false).build())
        .defaultInputModes(List.of("text"))
        .defaultOutputModes(List.of("text"))
        .skills(List.of(leaveOperations))
        .build();
  }

  @Bean
  public io.a2a.server.agentexecution.AgentExecutor agentExecutor(LeaveAgent leaveAgent) {
    return new com.google.adk.a2a.executor.AgentExecutor.Builder()
        .agent(leaveAgent.build())
        .appName("leave-agent")
        .sessionService(new InMemorySessionService())
        .artifactService(new InMemoryArtifactService())
        .agentExecutorConfig(AgentExecutorConfig.builder().build())
        .build();
  }
}
