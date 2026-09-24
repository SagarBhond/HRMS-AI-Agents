package com.shrija.payroll.config;

import com.google.adk.tools.mcp.McpToolset;
import com.shrija.payroll.mcp.PayrollMcpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayrollMcpConfig {

  @Bean(name = "payrollMcpToolset")
  public McpToolset payrollMcpToolset(
      PayrollAiProperties properties, PayrollMcpClient payrollMcpClient) {
    return payrollMcpClient.createToolset(properties.mcpServerUrl());
  }
}
