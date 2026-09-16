package com.hrms.mcpserver.tools;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class AssetTools {
  private final AtomicLong ids = new AtomicLong(1);
  private final Map<Long, AssetRecord> assets = new ConcurrentHashMap<>();

  @Tool(description = "Create an asset")
  public AssetRecord createAsset(String assetTag, String assetType, String description) {
    long id = ids.getAndIncrement();
    AssetRecord a = new AssetRecord(id, assetTag, assetType, description, null, "AVAILABLE", Instant.now().toString());
    assets.put(id, a); return a;
  }

  @Tool(description = "Get an asset")
  public AssetRecord getAsset(Long assetId) {
    AssetRecord a = assets.get(assetId);
    if (a == null) throw new IllegalArgumentException("No asset found with id " + assetId);
    return a;
  }

  @Tool(description = "List assets")
  public List<AssetRecord> listAssets() { return assets.values().stream().toList(); }

  @Tool(description = "Assign an asset to an employee")
  public AssetRecord assignAsset(Long assetId, Long employeeId) {
    AssetRecord a = getAsset(assetId);
    AssetRecord n = new AssetRecord(a.assetId(), a.assetTag(), a.assetType(), a.description(), employeeId, "ASSIGNED", a.createdAt());
    assets.put(assetId, n); return n;
  }

  @Tool(description = "Return an asset")
  public AssetRecord returnAsset(Long assetId) {
    AssetRecord a = getAsset(assetId);
    AssetRecord n = new AssetRecord(a.assetId(), a.assetTag(), a.assetType(), a.description(), null, "AVAILABLE", a.createdAt());
    assets.put(assetId, n); return n;
  }

  @Tool(description = "Update asset")
  public AssetRecord updateAsset(Long assetId, String assetTag, String assetType, String description) {
    AssetRecord a = getAsset(assetId);
    AssetRecord n = new AssetRecord(assetId, assetTag == null ? a.assetTag() : assetTag,
        assetType == null ? a.assetType() : assetType,
        description == null ? a.description() : description, a.employeeId(), a.status(), a.createdAt());
    assets.put(assetId, n); return n;
  }

  @Tool(description = "Get all assets assigned to an employee")
  public List<AssetRecord> getEmployeeAssets(Long employeeId) {
    return assets.values().stream().filter(a -> employeeId.equals(a.employeeId())).toList();
  }

  @Tool(description = "Update asset status")
  public AssetRecord updateAssetStatus(Long assetId, String status) {
    AssetRecord a = getAsset(assetId);
    AssetRecord n = new AssetRecord(a.assetId(), a.assetTag(), a.assetType(), a.description(), a.employeeId(), status, a.createdAt());
    assets.put(assetId, n); return n;
  }

  @Tool(description = "Mark an asset as lost")
  public AssetRecord markAssetLost(Long assetId) {
    return updateAssetStatus(assetId, "LOST");
  }

  @Tool(description = "Get assignment history for an asset (assign/return/status events)")
  public List<AssetEvent> getAssetHistory(Long assetId) {
    AssetRecord a = getAsset(assetId);
    List<AssetEvent> history = new java.util.ArrayList<>();
    history.add(new AssetEvent(assetId, "CREATED", a.createdAt()));
    if (a.employeeId() != null) history.add(new AssetEvent(assetId, "ASSIGNED_TO:" + a.employeeId(), Instant.now().toString()));
    history.add(new AssetEvent(assetId, "CURRENT_STATUS:" + a.status(), Instant.now().toString()));
    return history;
  }

  @Tool(description = "List all assets currently available (not assigned)")
  public List<AssetRecord> getAvailableAssets() {
    return assets.values().stream().filter(a -> "AVAILABLE".equals(a.status())).toList();
  }

  @Tool(description = "Generate an asset report summarizing counts by status")
  public AssetReport generateAssetReport() {
    List<AssetRecord> all = listAssets();
    long available = all.stream().filter(a -> "AVAILABLE".equals(a.status())).count();
    long assigned = all.stream().filter(a -> "ASSIGNED".equals(a.status())).count();
    long lost = all.stream().filter(a -> "LOST".equals(a.status())).count();
    return new AssetReport(all.size(), available, assigned, lost);
  }

  public record AssetRecord(Long assetId, String assetTag, String assetType, String description, Long employeeId, String status, String createdAt) {}
  public record AssetEvent(Long assetId, String event, String timestamp) {}
  public record AssetReport(long totalAssets, long available, long assigned, long lost) {}
}
