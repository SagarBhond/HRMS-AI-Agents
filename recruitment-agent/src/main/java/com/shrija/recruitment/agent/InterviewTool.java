package com.shrija.recruitment.agent;

/**
 * Documents the InterviewTool capability boundary.
 *
 * <p>Actual execution is provided by the shared MCP server through RecruitmentMcpClient.
 * This class does not duplicate recruitment business logic.
 */
public final class InterviewTool {
  private InterviewTool() {}
  public static final String[] FUNCTIONS = {"scheduleInterview", "getInterviewSchedule"};
}
