package com.shrija.asset.tool;

import com.google.common.collect.ImmutableList;
import java.util.List;

/** Groups asset return and lost-asset capabilities. */
public final class AssetReturnTool {
  private AssetReturnTool() {}

  public static final String RETURN_ASSET = "returnAsset";
  public static final String MARK_ASSET_LOST = "markAssetLost";

  public static List<String> toolNames() {
    return ImmutableList.of(RETURN_ASSET, MARK_ASSET_LOST);
  }
}
