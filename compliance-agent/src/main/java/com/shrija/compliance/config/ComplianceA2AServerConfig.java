package com.shrija.compliance.config;
import com.google.adk.a2a.executor.AgentExecutorConfig;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.sessions.InMemorySessionService;
import com.shrija.compliance.agent.ComplianceAgent;
import io.a2a.spec.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration public class ComplianceA2AServerConfig {
 @Bean public AgentCard agentCard(@Value("${server.port:8097}") int port){
  AgentSkill skill=new AgentSkill.Builder().id("compliance-management").name("Compliance Validation")
   .description("Validates access, policy compliance, sensitive operations, data exposure and document compliance through MCP.")
   .tags(List.of("compliance","authorization","security","policy","data-exposure","hrms")).build();
  return new AgentCard.Builder().name("compliance-agent").description("Compliance Agent for authorization and compliance validation.")
   .url("http://localhost:"+port).version("1.0.0").protocolVersion("0.3.0")
   .capabilities(new AgentCapabilities.Builder().streaming(false).build())
   .defaultInputModes(List.of("text")).defaultOutputModes(List.of("text")).skills(List.of(skill)).build();
 }
 @Bean public io.a2a.server.agentexecution.AgentExecutor agentExecutor(ComplianceAgent agent){
  return new com.google.adk.a2a.executor.AgentExecutor.Builder().agent(agent.build()).appName("compliance-agent")
   .sessionService(new InMemorySessionService()).artifactService(new InMemoryArtifactService())
   .agentExecutorConfig(AgentExecutorConfig.builder().build()).build();
 }
}
