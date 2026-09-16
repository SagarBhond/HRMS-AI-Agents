# Asset Agent

Google ADK + Spring Boot HRMS asset agent.

## Runtime architecture
- Gemini reasons over the user request.
- Business operations are exposed through the MCP server.
- The ADK agent registers the MCP toolset using `.tools(ImmutableList.of(assetMcpToolset))`.
- A2A exposes this agent to the rest of the HRMS agent network.
- `POST /api/v1/asset/chat` provides the direct chat endpoint.

## MCP tools expected
- `createAsset`
- `getAsset`
- `listAssets`
- `assignAsset`
- `returnAsset`
- `updateAsset`
- `getEmployeeAssets`
- `updateAssetStatus`

These names must match the actual MCP server tool names.
