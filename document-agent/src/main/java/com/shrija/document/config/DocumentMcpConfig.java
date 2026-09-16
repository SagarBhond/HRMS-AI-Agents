package com.shrija.document.config;

import com.google.adk.tools.mcp.McpToolset;
import com.shrija.document.mcp.DocumentMcpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentMcpConfig {
  @Bean(name = "documentMcpToolset")
  public McpToolset documentMcpToolset(
      DocumentAiProperties properties, DocumentMcpClient documentMcpClient) {
    return documentMcpClient.createToolset(properties.mcpServerUrl());
  }
}
