package com.shrija.asset.tool;

import com.google.common.collect.ImmutableList;
import java.util.List;

/** Groups asset history and reporting capabilities. */
public final class AssetHistoryTool {
  private AssetHistoryTool() {}

  public static final String GET_ASSET_HISTORY = "getAssetHistory";
  public static final String GENERATE_ASSET_REPORT = "generateAssetReport";

  public static List<String> toolNames() {
    return ImmutableList.of(GET_ASSET_HISTORY, GENERATE_ASSET_REPORT);
  }
}
