package com.shrija.workflow.config;
import com.google.adk.tools.mcp.McpToolset;
import com.shrija.workflow.mcp.WorkflowMcpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration public class WorkflowMcpConfig {
 @Bean(name="workflowMcpToolset") public McpToolset workflowMcpToolset(WorkflowAiProperties p,WorkflowMcpClient c){return c.createToolset(p.mcpServerUrl());}
}
