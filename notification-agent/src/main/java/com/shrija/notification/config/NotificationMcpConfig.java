package com.shrija.notification.config;

import com.google.adk.tools.mcp.McpToolset;
import com.shrija.notification.mcp.NotificationMcpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationMcpConfig {
  @Bean(name = "notificationMcpToolset")
  public McpToolset notificationMcpToolset(
      NotificationAiProperties properties, NotificationMcpClient notificationMcpClient) {
    return notificationMcpClient.createToolset(properties.mcpServerUrl());
  }
}
