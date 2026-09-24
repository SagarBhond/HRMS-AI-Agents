package com.shrija.recruitment.agent;

/**
 * Documents the JobOpeningTool capability boundary.
 *
 * <p>Actual execution is provided by the shared MCP server through RecruitmentMcpClient.
 * This class does not duplicate recruitment business logic.
 */
public final class JobOpeningTool {
  private JobOpeningTool() {}
  public static final String[] FUNCTIONS = {"createJobOpening", "updateJobOpening", "closeJobOpening"};
}
