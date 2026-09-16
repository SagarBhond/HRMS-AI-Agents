package com.shrija.performance.agent;
/** Documents the PerformanceReviewTool capability boundary. Actual execution is provided by the shared MCP server. */
public final class PerformanceReviewTool {
  private PerformanceReviewTool() {}
  public static final String[] FUNCTIONS = {"submitSelfReview", "createPerformanceReview", "getPerformanceReview"};
}
