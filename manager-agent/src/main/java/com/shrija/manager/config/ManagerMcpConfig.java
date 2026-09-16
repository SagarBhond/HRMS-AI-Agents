package com.shrija.manager.config;

import com.google.adk.tools.mcp.McpToolset;
import com.shrija.manager.mcp.ManagerMcpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ManagerMcpConfig {
  @Bean(name = "managerMcpToolset")
  public McpToolset managerMcpToolset(ManagerAiProperties properties, ManagerMcpClient client) {
    return client.createToolset(properties.mcpServerUrl());
  }
}
