package com.shrija.audit.config;
import com.google.adk.tools.mcp.McpToolset;
import com.shrija.audit.mcp.AuditMcpClient;
import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration;
@Configuration public class AuditMcpConfig { @Bean(name="auditMcpToolset") public McpToolset auditMcpToolset(AuditAiProperties p,AuditMcpClient c) { return c.createToolset(p.mcpServerUrl()); } }
