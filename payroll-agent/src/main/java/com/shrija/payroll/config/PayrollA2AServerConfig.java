package com.shrija.payroll.config;

import com.google.adk.a2a.executor.AgentExecutorConfig;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.sessions.InMemorySessionService;
import com.shrija.payroll.agent.PayrollAgent;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayrollA2AServerConfig {

  @Bean
  public AgentCard agentCard(@Value("${server.port:8085}") int port) {
    AgentSkill salarySkill =
        new AgentSkill.Builder()
            .id("payroll-operations")
            .name("Payroll Calculations, Salary Slips & Reports")
            .description(
                "Calculate salary components, generate and retrieve salary slips, mark slips paid, "
                    + "retrieve payroll history and summaries, generate payroll reports and explain salary breakdowns. "
                    + "All data access goes through the shared MCP server.")
            .tags(List.of("payroll", "salary", "payslip", "tax", "deductions", "reports", "hrms"))
            .build();

    return new AgentCard.Builder()
        .name("payroll-agent")
        .description(
            "Payroll Agent – salary calculations, salary slips, payroll history, summaries, reports and salary explanations. "
                + "All data access is performed exclusively through MCP.")
        .url("http://localhost:" + port)
        .version("1.0.0")
        .protocolVersion("0.3.0")
        .capabilities(new AgentCapabilities.Builder().streaming(false).build())
        .defaultInputModes(List.of("text"))
        .defaultOutputModes(List.of("text"))
        .skills(List.of(salarySkill))
        .build();
  }

  @Bean
  public io.a2a.server.agentexecution.AgentExecutor agentExecutor(PayrollAgent payrollAgent) {
    return new com.google.adk.a2a.executor.AgentExecutor.Builder()
        .agent(payrollAgent.build())
        .appName("payroll-agent")
        .sessionService(new InMemorySessionService())
        .artifactService(new InMemoryArtifactService())
        .agentExecutorConfig(AgentExecutorConfig.builder().build())
        .build();
  }
}
