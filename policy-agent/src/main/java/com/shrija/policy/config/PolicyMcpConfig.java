package com.shrija.policy.config;

import com.google.adk.tools.mcp.McpToolset;
import com.shrija.policy.mcp.PolicyMcpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PolicyMcpConfig {

  @Bean(name = "policyMcpToolset")
  public McpToolset policyMcpToolset(
      PolicyAiProperties properties, PolicyMcpClient policyMcpClient) {
    return policyMcpClient.createToolset(properties.mcpServerUrl());
  }
}
