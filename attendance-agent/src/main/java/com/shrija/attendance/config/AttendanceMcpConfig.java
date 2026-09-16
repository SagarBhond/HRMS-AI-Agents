package com.shrija.attendance.config;

import com.google.adk.tools.mcp.McpToolset;
import com.shrija.attendance.mcp.AttendanceMcpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AttendanceMcpConfig {

  @Bean(name = "attendanceMcpToolset")
  public McpToolset attendanceMcpToolset(
      AttendanceAiProperties properties, AttendanceMcpClient attendanceMcpClient) {
    return attendanceMcpClient.createToolset(properties.mcpServerUrl());
  }
}
