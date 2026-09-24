package com.shrija.expense.config;

import com.google.adk.a2a.executor.AgentExecutorConfig;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.sessions.InMemorySessionService;
import com.shrija.expense.agent.ExpenseAgent;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExpenseA2AServerConfig {

  @Bean
  public AgentCard agentCard(@Value("${server.port:8092}") int port) {
    AgentSkill skill =
        new AgentSkill.Builder()
            .id("expense-management")
            .name("Expense Management")
            .description(
                "Submit, retrieve, approve, reject, reimburse, report and hand off expenses to Payroll.")
            .tags(List.of("expense", "reimbursement", "approval", "payroll"))
            .build();

    return new AgentCard.Builder()
        .name("expense-agent")
        .description(
            "Expense Agent for HRMS expense management backed by the shared Expense MCP tools.")
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
  public io.a2a.server.agentexecution.AgentExecutor agentExecutor(ExpenseAgent agent) {
    return new com.google.adk.a2a.executor.AgentExecutor.Builder()
        .agent(agent.build())
        .appName("expense-agent")
        .sessionService(new InMemorySessionService())
        .artifactService(new InMemoryArtifactService())
        .agentExecutorConfig(AgentExecutorConfig.builder().build())
        .build();
  }
}
