package com.shrija.asset.config;

import com.google.adk.tools.mcp.McpToolset;
import com.shrija.asset.mcp.AssetMcpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AssetMcpConfig {

  @Bean(name = "assetMcpToolset")
  public McpToolset assetMcpToolset(
      AssetAiProperties properties, AssetMcpClient assetMcpClient) {
    return assetMcpClient.createToolset(properties.mcpServerUrl());
  }
}
