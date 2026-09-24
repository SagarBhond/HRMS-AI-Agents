package com.shrija.expense.config;

import com.google.adk.tools.mcp.McpToolset;
import com.shrija.expense.mcp.ExpenseMcpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExpenseMcpConfig {

  @Bean(name = "expenseMcpToolset")
  public McpToolset expenseMcpToolset(
      ExpenseAiProperties properties, ExpenseMcpClient expenseMcpClient) {
    return expenseMcpClient.createToolset(properties.mcpServerUrl());
  }
}
