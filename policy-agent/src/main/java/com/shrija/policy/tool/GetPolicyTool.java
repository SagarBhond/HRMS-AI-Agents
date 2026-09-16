package com.shrija.policy.tool;

import com.google.common.collect.ImmutableList;
import java.util.List;

/** Retrieves the complete policy record from the Policy MCP source of truth. */
public final class GetPolicyTool {
  private GetPolicyTool() {}

  public static final String MCP_TOOL_NAME = "getPolicy";

  public static List<String> toolNames() {
    return ImmutableList.of(MCP_TOOL_NAME);
  }
}
