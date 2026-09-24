# Employee Agent

Standalone Employee Agent for the Shrija HRMS.

## Runtime

- Java: 21
- Spring Boot: 4.0.2
- Google ADK: 1.6.1-SNAPSHOT
- Port: 8081
- MCP server: `http://localhost:8082/mcp`
- A2A transport: JSON-RPC client using the project's `google-adk-a2a` module

## Scope

The agent owns employee identity/profile information, contact information, department,
designation, manager relationships, reporting hierarchy, employment status, employee lookup,
search, create and update operations.

It never connects to MySQL. All employee data operations are exposed by the existing shared
`mcp-shrija-server` and filtered through the Employee Agent's MCP allowlist.

## API

`POST /api/v1/employee/chat`

```json
{
  "userId": "EMP1024",
  "role": "EMPLOYEE",
  "message": "Show my profile"
}
```

## Environment

- `GOOGLE_API_KEY`
- `EMPLOYEE_GEMINI_MODEL` (default: `gemini-3.1-flash-lite`)
- `EMPLOYEE_MCP_SERVER_URL` (default: `http://localhost:8082/mcp`)
- `ATTENDANCE_AGENT_A2A_URL` (default: `http://localhost:8084`)
- `LEAVE_AGENT_A2A_URL` (default: `http://localhost:8087`)
- `PAYROLL_AGENT_A2A_URL` (default: `http://localhost:8089`)
- `MANAGER_AGENT_A2A_URL` (default: `http://localhost:8080`)

## Start order

1. Start `mcp-shrija-server` on port 8082.
2. Start `employee-agent` on port 8081.
3. Start any target A2A agents before invoking their Employee Agent clients.

## Error handling

All failures return the same JSON shape from `EmployeeAgentExceptionHandler`:

```json
{
  "timestamp": "2026-09-18T10:12:33.412Z",
  "traceId": "9f2c1ab4",
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed.",
  "path": "/api/v1/employee/chat",
  "details": ["message: must not be blank"]
}
```

`details` is present only for validation failures. `traceId` is logged with the full
stack trace, so a caller can quote it for support without the response exposing internals.

| Condition | Status |
|---|---|
| `@Valid` / constraint violation, malformed JSON body | 400 |
| `IllegalArgumentException` (unconfigured A2A URL, bad argument) | 400 |
| `SecurityException` (actor not authorized for the target employee) | 403 |
| Unsupported method / media type | 405 / 415 |
| `A2AAgentUnavailableException`, `IllegalStateException` (MCP/Gemini/session), `EmployeeAgentExecutionException` | 503 |
| Anything else | 500 (generic message only) |
