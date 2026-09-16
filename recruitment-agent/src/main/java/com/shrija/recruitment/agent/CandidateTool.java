package com.shrija.recruitment.agent;

/**
 * Documents the CandidateTool capability boundary.
 *
 * <p>Actual execution is provided by the shared MCP server through RecruitmentMcpClient.
 * This class does not duplicate recruitment business logic.
 */
public final class CandidateTool {
  private CandidateTool() {}
  public static final String[] FUNCTIONS = {"createCandidate", "searchCandidates", "shortlistCandidate", "rejectCandidate"};
}
