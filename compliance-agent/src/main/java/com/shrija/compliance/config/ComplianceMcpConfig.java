package com.shrija.compliance.config;
import com.google.adk.tools.mcp.McpToolset;
import com.shrija.compliance.mcp.ComplianceMcpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration public class ComplianceMcpConfig {
 @Bean(name="complianceMcpToolset") public McpToolset complianceMcpToolset(ComplianceAiProperties p,ComplianceMcpClient c){return c.createToolset(p.mcpServerUrl());}
}
