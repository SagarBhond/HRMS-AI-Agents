package com.shrija.employee.config;

import com.google.adk.tools.mcp.McpToolset;
import com.shrija.employee.exception.McpToolsetInitializationException;
import com.shrija.employee.mcp.EmployeeMcpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmployeeMcpConfig {

  private static final Logger log = LoggerFactory.getLogger(EmployeeMcpConfig.class);

  @Bean(name = "employeeMcpToolset")
  public McpToolset employeeMcpToolset(
      EmployeeAiProperties properties, EmployeeMcpClient employeeMcpClient) {
    String mcpServerUrl = properties.mcpServerUrl();
    try {
      return employeeMcpClient.createToolset(mcpServerUrl);
    } catch (RuntimeException ex) {
      // Same outcome as before (startup fails), but the log now names the URL and the cause
      // instead of surfacing a bare constructor exception from deep inside the ADK.
      log.error("Failed to build Employee MCP toolset for mcpServerUrl={}", mcpServerUrl, ex);
      throw new McpToolsetInitializationException(
          "Employee MCP toolset could not be initialised. Check shrija.employee.ai.mcp-server-url"
              + " and that the MCP server is reachable.",
          ex);
    }
  }
}
