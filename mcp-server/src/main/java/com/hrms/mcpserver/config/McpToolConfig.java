package com.hrms.mcpserver.config;

import com.hrms.mcpserver.tools.AssetTools;
import com.hrms.mcpserver.tools.AttendanceTools;
import com.hrms.mcpserver.tools.AuditTools;
import com.hrms.mcpserver.tools.ComplianceTools;
import com.hrms.mcpserver.tools.DocumentTools;
import com.hrms.mcpserver.tools.EmployeeTools;
import com.hrms.mcpserver.tools.ExpenseTools;
import com.hrms.mcpserver.tools.HrTools;
import com.hrms.mcpserver.tools.LeaveTools;
import com.hrms.mcpserver.tools.ManagerTools;
import com.hrms.mcpserver.tools.NotificationTools;
import com.hrms.mcpserver.tools.PayrollTools;
import com.hrms.mcpserver.tools.PerformanceTools;
import com.hrms.mcpserver.tools.PolicyTools;
import com.hrms.mcpserver.tools.RecruitmentTools;
import com.hrms.mcpserver.tools.WorkflowTools;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers every domain Tools class as one flat MCP tool pool, exposed over a single SSE endpoint.
 * Each agent connects to this same server and applies its own client-side ALLOWED_TOOLS allow-list
 * (see each agent's *McpClient) to pick only the tool names relevant to its role, so a tool defined
 * once here (e.g. LeaveTools#decideOnLeaveRequest) can be shared by more than one agent (Leave
 * Agent and Manager Agent) without duplication.
 */
@Configuration
public class McpToolConfig {

  @Bean
  public ToolCallbackProvider hrmsToolCallbackProvider(
      EmployeeTools employeeTools,
      AttendanceTools attendanceTools,
      ManagerTools managerTools,
      HrTools hrTools,
      LeaveTools leaveTools,
      PayrollTools payrollTools,
      DocumentTools documentTools,
      NotificationTools notificationTools,
      PolicyTools policyTools,
      ExpenseTools expenseTools,
      AssetTools assetTools,
      PerformanceTools performanceTools,
      RecruitmentTools recruitmentTools,
      AuditTools auditTools,
      ComplianceTools complianceTools,
      WorkflowTools workflowTools) {

    return MethodToolCallbackProvider.builder()
        .toolObjects(
            employeeTools,
            attendanceTools,
            managerTools,
            hrTools,
            leaveTools,
            payrollTools,
            documentTools,
            notificationTools,
            policyTools,
            expenseTools,
            assetTools,
            performanceTools,
            recruitmentTools,
            auditTools,
            complianceTools,
            workflowTools)
        .build();
  }

  @Bean
  public ToolCallback[] hrmsToolCallbacks(ToolCallbackProvider hrmsToolCallbackProvider) {
    return hrmsToolCallbackProvider.getToolCallbacks();
  }
}
