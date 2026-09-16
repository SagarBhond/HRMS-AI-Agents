package com.shrija.leave.config;
import com.google.adk.tools.mcp.McpToolset;
import com.shrija.leave.mcp.LeaveMcpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class LeaveMcpConfig {
  @Bean(name="leaveMcpToolset")
  public McpToolset leaveMcpToolset(LeaveAiProperties properties,LeaveMcpClient client){return client.createToolset(properties.mcpServerUrl());}
}
