package com.shrija.performance.config;
import com.google.adk.tools.mcp.McpToolset;
import com.shrija.performance.mcp.PerformanceMcpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class PerformanceMcpConfig {
  @Bean(name="performanceMcpToolset")
  public McpToolset performanceMcpToolset(PerformanceAiProperties properties, PerformanceMcpClient client) {
    return client.createToolset(properties.mcpServerUrl());
  }
}
