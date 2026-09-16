package com.hrms.mcpserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Single shared MCP server for the HRMS project. Exposes MCP tools for all 5 domains (Employee,
 * Leave, Attendance, Payroll, HR) over one SSE endpoint, backed by one MySQL database. The
 * Orchestrator Agent (Java ADK) connects to this as an MCP client.
 */
@SpringBootApplication
public class McpServerApplication {
  public static void main(String[] args) {
    SpringApplication.run(McpServerApplication.class, args);
  }
}
