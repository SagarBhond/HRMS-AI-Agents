package com.shrija.recruitment.config;
import com.google.adk.tools.mcp.McpToolset;
import com.shrija.recruitment.mcp.RecruitmentMcpClient;
import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration;
@Configuration public class RecruitmentMcpConfig { @Bean(name="recruitmentMcpToolset") public McpToolset recruitmentMcpToolset(RecruitmentAiProperties p,RecruitmentMcpClient c) { return c.createToolset(p.mcpServerUrl()); } }
