# Expense Agent

Google ADK + Spring Boot HRMS expense agent.

## Runtime architecture
- Gemini reasons over the user request.
- Business operations are exposed through the MCP server.
- The ADK agent registers the MCP toolset using `.tools(ImmutableList.of(expenseMcpToolset))`.
- A2A exposes this agent to the rest of the HRMS agent network.
- `POST /api/v1/expense/chat` provides the direct chat endpoint.

## MCP tools expected
- `submitExpense`
- `getExpense`
- `listExpenses`
- `validateExpense`
- `approveExpense`
- `rejectExpense`
- `calculateReimbursement`
- `getReimbursementStatus`

These names must match the actual MCP server tool names.
