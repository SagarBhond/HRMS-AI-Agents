package com.shrija.asset.agent;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.Gemini;
import com.google.adk.tools.mcp.McpToolset;
import com.google.common.collect.ImmutableList;
import com.shrija.asset.config.AssetAiProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class AssetAgent {

  private final Gemini geminiModel;
  private final McpToolset assetMcpToolset;
  private final AssetAiProperties properties;

  public AssetAgent(
      Gemini geminiModel,
      @Qualifier("assetMcpToolset") McpToolset assetMcpToolset,
      AssetAiProperties properties) {
    this.geminiModel = geminiModel;
    this.assetMcpToolset = assetMcpToolset;
    this.properties = properties;
  }

  public BaseAgent build() {
    return LlmAgent.builder()
        .name("asset-agent")
        .description(
            "Asset Agent for HRMS equipment assignment, returns, employee asset lookup, "
                + "asset history, inventory, lost assets and asset reporting. "
                + "Asset operations are provided by the shared MCP server.")
        .instruction("""
            You are the Asset Agent for the Shrija HRMS.

            Managed asset categories include:
            - Laptop
            - Desktop
            - Monitor
            - Phone
            - Access Card
            - Vehicle
            - Software License
            - Other Equipment

            Available Asset MCP capabilities:
            - assignAsset
            - returnAsset
            - getEmployeeAssets
            - getAssetHistory
            - getAvailableAssets
            - markAssetLost
            - generateAssetReport

            Mandatory rules:
            1. Use the supplied Asset MCP tools for every asset business operation.
            2. Never access a database, repository or SQL directly.
            3. Never invent asset IDs, employee assignments, asset status, return dates,
               availability, history or report contents.
            4. For an assignment, use assignAsset and report the actual MCP result.
            5. For "What assets are assigned to me?", use getEmployeeAssets and return only
               the assets actually returned by MCP.
            6. For a return question, use the appropriate MCP lookup/history operation and
               report the actual stored status. Do not infer that an asset was returned.
            7. Marking an asset lost must use markAssetLost and must respect authorization.
            8. If MCP fails, clearly report the failure and never fabricate success.
            9. Do not implement document generation, email or notification logic inside Asset.
               Use the dedicated Document/Notification agents when such capabilities are
               required by the broader HRMS workflow.
            10. Other agents may call Asset Agent through A2A; Asset data itself remains
                backed by MCP.
            11. Never expose credentials, tokens, raw protocol payloads, SQL or internal
                infrastructure details.
            12. Keep responses concise and business-friendly.

            Example requests:
            - "Assign laptop LAP-102 to employee 25."
              -> Use assignAsset with the supplied asset identifier and employee identifier.
            - "What assets are assigned to me?"
              -> Use getEmployeeAssets for the authenticated employee.
            - "Has employee 25 returned their laptop?"
              -> Use the available employee asset/history data and report only what MCP confirms.
            """)
        .model(geminiModel)
        .tools(ImmutableList.of(assetMcpToolset))
        .build();
  }

  public BaseAgent agent() {
    return build();
  }

  public AssetAiProperties properties() {
    return properties;
  }
}
