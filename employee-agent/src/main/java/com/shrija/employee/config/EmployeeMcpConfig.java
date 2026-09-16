package com.shrija.employee.config;

import com.google.adk.tools.mcp.McpToolset;
import com.shrija.employee.mcp.EmployeeMcpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmployeeMcpConfig {

  @Bean(name = "employeeMcpToolset")
  public McpToolset employeeMcpToolset(
      EmployeeAiProperties properties, EmployeeMcpClient employeeMcpClient) {
    return employeeMcpClient.createToolset(properties.mcpServerUrl());
  }
}
