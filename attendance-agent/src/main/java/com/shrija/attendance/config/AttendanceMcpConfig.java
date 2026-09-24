package com.shrija.attendance.config;

import com.google.adk.tools.mcp.McpToolset;
import com.shrija.attendance.exception.McpToolsetInitializationException;
import com.shrija.attendance.mcp.AttendanceMcpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AttendanceMcpConfig {

  private static final Logger log = LoggerFactory.getLogger(AttendanceMcpConfig.class);

  @Bean(name = "attendanceMcpToolset")
  public McpToolset attendanceMcpToolset(
      AttendanceAiProperties properties, AttendanceMcpClient attendanceMcpClient) {
    String mcpServerUrl = properties.mcpServerUrl();
    try {
      return attendanceMcpClient.createToolset(mcpServerUrl);
    } catch (RuntimeException ex) {
      // Same outcome as before (startup fails), but the log now names the URL and the cause
      // instead of surfacing a bare constructor exception from deep inside the ADK.
      log.error("Failed to build Attendance MCP toolset for mcpServerUrl={}", mcpServerUrl, ex);
      throw new McpToolsetInitializationException(
          "Attendance MCP toolset could not be initialised. Check shrija.ai.mcp-server-url and"
              + " that the MCP server is reachable.",
          ex);
    }
  }
}
