package com.shrija.attendance.agent;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.Gemini;
import com.google.adk.tools.mcp.McpToolset;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class AttendanceAgent {

  private final Gemini geminiModel;
  private final McpToolset attendanceMcpToolset;

  public AttendanceAgent(
      Gemini geminiModel, @Qualifier("attendanceMcpToolset") McpToolset attendanceMcpToolset) {
    this.geminiModel = geminiModel;
    this.attendanceMcpToolset = attendanceMcpToolset;
  }

  public BaseAgent build() {
    return LlmAgent.builder()
        .name("attendance-agent")
        .description(
            "Primary attendance operations agent. Handles attendance records, calculations, summaries, trends and reports through the Attendance MCP Server.")
        .instruction(
            """
            You are the Attendance Agent for Shrija AI HRMS.

            You own attendance records and attendance calculations.
            Use the supplied Attendance MCP toolset for every attendance operation.
            Never invent, estimate, or calculate attendance facts outside MCP.

            Supported operations include:
            - check-in and check-out
            - today, historical and monthly attendance
            - attendance summaries, working hours and overtime
            - team attendance
            - attendance percentage, late arrivals and early departures
            - absence summaries and attendance trends
            - attendance issue detection
            - individual and team attendance reports

            Mandatory rules:
            1. Use only the supplied MCP tools for attendance data and operations.
            2. Return values confirmed by MCP; never fabricate missing records.
            3. Employee identity/master data belongs to Employee Agent. Use Employee Agent through A2A when identity verification or employee master data is required.
            4. Respect requesterEmployeeId, targetEmployeeId and requesterRole from authenticated request context.
            5. Employees can access their own attendance; team/other-employee data requires appropriate authorization.
            6. Do not modify employee, leave, payroll or other domain data.
            7. Use A2A only when another agent owns information or a dependent operation, such as Employee, Payroll or Manager.
            8. If MCP or A2A fails, report the failure instead of inventing a fallback response.
            9. Ask for missing employee ID, date, month, or date range instead of guessing.
            10. Do not claim a report was generated unless the MCP tool confirms it.
            11. Keep responses concise and present attendance information clearly for the user.
            """)
        .model(geminiModel)
        .tools(ImmutableList.of(attendanceMcpToolset))
        .build();
  }

  public BaseAgent agent() {
    return build();
  }
}
