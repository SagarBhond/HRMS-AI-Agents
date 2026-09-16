package com.shrija.workflow.config;
import com.google.adk.a2a.executor.AgentExecutorConfig;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.sessions.InMemorySessionService;
import com.shrija.workflow.agent.WorkflowAgent;
import io.a2a.spec.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration public class WorkflowA2AServerConfig {
 @Bean public AgentCard agentCard(@Value("${server.port:8098}") int port){
  AgentSkill skill=new AgentSkill.Builder().id("workflow-management").name("HR Business Workflow Management")
   .description("Creates and manages onboarding, offboarding and other HR business workflows through MCP.")
   .tags(List.of("workflow","onboarding","offboarding","approvals","hrms")).build();
  return new AgentCard.Builder().name("workflow-agent").description("Workflow Agent for HR business process orchestration.")
   .url("http://localhost:"+port).version("1.0.0").protocolVersion("0.3.0")
   .capabilities(new AgentCapabilities.Builder().streaming(false).build())
   .defaultInputModes(List.of("text")).defaultOutputModes(List.of("text")).skills(List.of(skill)).build();
 }
 @Bean public io.a2a.server.agentexecution.AgentExecutor agentExecutor(WorkflowAgent agent){
  return new com.google.adk.a2a.executor.AgentExecutor.Builder().agent(agent.build()).appName("workflow-agent")
   .sessionService(new InMemorySessionService()).artifactService(new InMemoryArtifactService())
   .agentExecutorConfig(AgentExecutorConfig.builder().build()).build();
 }
}
