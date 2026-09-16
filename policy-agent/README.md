# Policy Agent

Google ADK + Spring Boot HRMS policy agent.

## Runtime architecture
- Gemini reasons over the user request.
- Business operations are exposed through the MCP server.
- The ADK agent registers the MCP toolset using `.tools(ImmutableList.of(policyMcpToolset))`.
- A2A exposes this agent to the rest of the HRMS agent network.
- `POST /api/v1/policy/chat` provides the direct chat endpoint.

## MCP tools expected
- `createPolicy`
- `updatePolicy`
- `getPolicy`
- `listPolicies`
- `validatePolicy`
- `checkPolicyEligibility`

These names must match the actual MCP server tool names.
