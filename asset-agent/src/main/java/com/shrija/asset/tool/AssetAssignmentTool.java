package com.shrija.asset.tool;

import com.google.common.collect.ImmutableList;
import java.util.List;

/** Groups asset assignment capabilities owned by the Asset Agent. */
public final class AssetAssignmentTool {
  private AssetAssignmentTool() {}

  public static final String ASSIGN_ASSET = "assignAsset";
  public static final String GET_EMPLOYEE_ASSETS = "getEmployeeAssets";

  public static List<String> toolNames() {
    return ImmutableList.of(ASSIGN_ASSET, GET_EMPLOYEE_ASSETS);
  }
}
