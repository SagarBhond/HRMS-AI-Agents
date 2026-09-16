package com.shrija.asset.tool;

import com.google.common.collect.ImmutableList;
import java.util.List;

/** Groups asset inventory lookup capabilities. */
public final class AssetInventoryTool {
  private AssetInventoryTool() {}

  public static final String GET_AVAILABLE_ASSETS = "getAvailableAssets";

  public static List<String> toolNames() {
    return ImmutableList.of(GET_AVAILABLE_ASSETS);
  }
}
