package com.shrija.recruitment.agent;

/**
 * Documents the RecruitmentPipelineTool capability boundary.
 *
 * <p>Actual execution is provided by the shared MCP server through RecruitmentMcpClient.
 * This class does not duplicate recruitment business logic.
 */
public final class RecruitmentPipelineTool {
  private RecruitmentPipelineTool() {}
  public static final String[] FUNCTIONS = {"moveCandidateStage"};
}
