package com.shrija.performance.agent;
/** Documents the GoalTool capability boundary. Actual execution is provided by the shared MCP server. */
public final class GoalTool {
  private GoalTool() {}
  public static final String[] FUNCTIONS = {"createGoal", "updateGoal", "getGoals", "completeGoal"};
}
