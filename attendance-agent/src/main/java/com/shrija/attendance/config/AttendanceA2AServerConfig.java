package com.shrija.attendance.config;

import com.google.adk.a2a.executor.AgentExecutorConfig;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.sessions.InMemorySessionService;
import com.shrija.attendance.agent.AttendanceAgent;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes attendance-agent as an A2A server, mirroring EmployeeA2AServerConfig in employee-agent.
 * Without this, attendance-agent can only ever be an A2A *client* (calling out to Employee/Payroll/
 * Manager agents) and can never itself be discovered or invoked over A2A by any other agent.
 */
@Configuration
public class AttendanceA2AServerConfig {

  @Bean
  public AgentCard agentCard(@Value("${server.port:8084}") int port) {
    AgentSkill attendanceOps =
        new AgentSkill.Builder()
            .id("attendance-operations")
            .name("Attendance Check-In/Out & Reporting")
            .description(
                "Handles employee check-in, check-out, today's attendance, attendance history, "
                    + "monthly attendance, working hours, overtime, and team attendance reporting "
                    + "through the Attendance MCP server.")
            .tags(List.of("attendance", "hrms", "checkin", "checkout", "overtime"))
            .build();

    return new AgentCard.Builder()
        .name("attendance-agent")
        .description(
            "Handles HRMS attendance operations (check-in/out, history, reports) through MCP "
                + "and coordinates with Employee, Payroll, and Manager agents through A2A.")
        .url("http://localhost:" + port)
        .version("1.0.0")
        .protocolVersion("0.3.0")
        .capabilities(new AgentCapabilities.Builder().streaming(false).build())
        .defaultInputModes(List.of("text"))
        .defaultOutputModes(List.of("text"))
        .skills(List.of(attendanceOps))
        .build();
  }

  // Bridges the A2A protocol layer directly into the existing ADK AttendanceAgent, same pattern
  // as employee-agent's AgentExecutor bean.
  @Bean
  public io.a2a.server.agentexecution.AgentExecutor agentExecutor(AttendanceAgent attendanceAgent) {
    return new com.google.adk.a2a.executor.AgentExecutor.Builder()
        .agent(attendanceAgent.agent())
        .appName("attendance-agent")
        .sessionService(new InMemorySessionService())
        .artifactService(new InMemoryArtifactService())
        .agentExecutorConfig(AgentExecutorConfig.builder().build())
        .build();
  }
}
