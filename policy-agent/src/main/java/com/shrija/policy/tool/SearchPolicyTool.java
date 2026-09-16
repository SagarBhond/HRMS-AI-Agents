package com.shrija.policy.tool;

import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * Policy search capability.
 *
 * <p>The current MCP server exposes {@code listPolicies} as the source-of-truth
 * retrieval operation. The Policy Agent uses that result to locate the relevant
 * published policy and then retrieves the selected policy with {@code getPolicy}.
 */
public final class SearchPolicyTool {
  private SearchPolicyTool() {}

  public static final String MCP_TOOL_NAME = "listPolicies";

  public static List<String> toolNames() {
    return ImmutableList.of(MCP_TOOL_NAME);
  }
}
