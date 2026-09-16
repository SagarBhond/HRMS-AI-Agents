package com.shrija.asset.tool;

import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Documents the business capabilities exposed to the Asset Agent.
 * Runtime execution is intentionally provided by the MCP server via assetMcpToolset.
 */
@Component
public class AssetToolCatalog {
  public List<String> operations() {
    return List.of("createAsset", "getAsset", "listAssets", "assignAsset", "returnAsset", "updateAsset", "getEmployeeAssets", "updateAssetStatus");
  }
}
