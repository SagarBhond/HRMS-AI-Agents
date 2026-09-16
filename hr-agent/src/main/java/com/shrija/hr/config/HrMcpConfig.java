package com.shrija.hr.config;

import com.google.adk.tools.mcp.McpToolset;
import com.shrija.hr.mcp.HrMcpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HrMcpConfig {
  @Bean(name = "hrMcpToolset")
  public McpToolset hrMcpToolset(HrAiProperties properties, HrMcpClient client) {
    return client.createToolset(properties.mcpServerUrl());
  }
}
