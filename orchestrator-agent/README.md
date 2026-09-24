# Orchestrator Agent

The top-level "ADK Service :8080" from the program flow:

```
React UI -> Auth Service :8081 (JWT) -> ADK Service :8080
  -> JWT Authentication Filter -> AuthenticatedUser
  -> ORCHESTRATION AGENT (Gemini LLM, semantic intent analysis + routing)
  -> ADK sub-agent delegation (A2A) -> Employee / Attendance / Payroll / HR / Leave / Manager / Budget / CTO
  -> MCP Toolsets (inside each domain agent) -> HRMS MCP Server
```

| Primary Responsibility            | Uses MCP                       | Communicates With |
|------------------------------------|---------------------------------|--------------------|
| Intent routing and workflow coordination | No (indirectly through agents) | All agents |

## Runtime

- Java: 21
- Spring Boot: 4.0.2
- Google ADK: 1.6.1-SNAPSHOT
- Port: `8080`
- No MCP server dependency of its own -- see "Design note" below.
- A2A transport: JSON-RPC, same `google-adk-a2a` module as every other agent

## What this module does

1. **JWT Authentication Filter** (`security/JwtAuthenticationFilter`) -- verifies the
   `Authorization: Bearer <JWT>` header issued by the Auth Service using a dependency-free HS256
   verifier (`security/JwtUtil`, built only on `javax.crypto`/`java.util.Base64` -- no third-party
   JWT library, so there's nothing new to resolve from a Maven repository). Rejects missing,
   malformed, wrongly-signed, or expired tokens with `401`.
2. **AuthenticatedUser** (`security/AuthenticatedUser`) -- the verified `userId`, `username`,
   `role`, `employeeCode`, attached to the request. This is the only place in the whole agent mesh
   where identity is cryptographically verified; every hop after this is trusted internal A2A
   traffic between agents on the private network, matching the original diagram's trust boundary.
3. **Orchestration Agent** (`agent/OrchestratorAgent`) -- a Gemini-backed `LlmAgent` with one
   delegation tool per domain agent (`tool/DelegationTools`). The LLM does semantic intent
   analysis and picks the right tool(s); the Java tool code -- not the LLM -- builds the trusted
   "Authenticated actor: ..." context line from `AuthenticatedUser` and forwards it verbatim to
   the target agent over A2A, so a downstream agent's own defense-in-depth authorization checks
   (see `attendance-agent`/`manager-agent`) still see a value nobody could spoof through the chat
   text.
4. **Controller** (`controller/OrchestratorController`) -- `POST /api/v1/orchestrator/chat`. The
   request body only ever carries `sessionId` + `message`; `userId`/`role`/`employeeCode` are
   deliberately *not* accepted from the client and are read only from the verified JWT.

## Design note: why delegation tools instead of native ADK `subAgents`

`employee-agent`'s `A2AAgentClientSupport.connect()` already builds a bare `RemoteA2AAgent` that
*could* be wired in as a native ADK sub-agent. The Orchestrator instead reuses
`attendance-agent`'s proven "resolve card -> open one-shot session -> collect text response"
pattern (`A2AAgentClientSupport.call(url, prompt)`), wrapped as ordinary `FunctionTool`s. Two
reasons:

- It's the pattern already exercised end-to-end in this codebase (`PayrollAgentClient`,
  `ManagerAgentClient` in `attendance-agent`), so it doesn't depend on unverified behavior of a
  native `subAgents()` builder method in this exact ADK snapshot.
- It lets each delegation gracefully degrade: `DelegationTools` catches a failed/unreachable
  target agent and returns `{"agent": "...", "error": "... is unavailable: ..."}` instead of
  throwing, so the Orchestrator can boot and answer requests for agents that *are* running even
  before Payroll, HR, Leave, Budget, and CTO agents exist yet.

## API

`POST /api/v1/orchestrator/chat`

```http
POST http://localhost:8080/api/v1/orchestrator/chat
Authorization: Bearer <JWT from Auth Service>
Content-Type: application/json

{
  "message": "Show my attendance for August and check if my leave was approved"
}
```

```json
{
  "sessionId": "b6b6...",
  "responseText": "Your August attendance ... Your leave request ..."
}
```

A missing/invalid/expired token returns:

```json
{ "timestamp": "...", "error": "JWT has expired." }
```

## Environment

- `GOOGLE_API_KEY`
- `ORCHESTRATOR_GEMINI_MODEL` (default: `gemini-3.1-flash-lite`)
- `JWT_SECRET` -- **must** match the Auth Service's signing secret exactly (HS256).
- `JWT_USER_ID_CLAIM` / `JWT_USERNAME_CLAIM` / `JWT_ROLE_CLAIM` / `JWT_EMPLOYEE_CODE_CLAIM` --
  override only if the Auth Service's JWT payload uses different claim names than `sub` /
  `username` / `role` / `employeeCode`.
- `EMPLOYEE_AGENT_A2A_URL` (default: `http://localhost:8083`)
- `ATTENDANCE_AGENT_A2A_URL` (default: `http://localhost:8084`)
- `PAYROLL_AGENT_A2A_URL` (default: `http://localhost:8085`)
- `MANAGER_AGENT_A2A_URL` (default: `http://localhost:8086`)
- `LEAVE_AGENT_A2A_URL` (default: `http://localhost:8087`)
- `HR_AGENT_A2A_URL` (default: `http://localhost:8088`)
- `BUDGET_AGENT_A2A_URL` (default: `http://localhost:8089`)
- `CTO_AGENT_A2A_URL` (default: `http://localhost:8090`)
- `ORCHESTRATOR_AGENT_PORT` (default: `8080`)

## Start order

1. Start `hrms-mcp-server` (8082).
2. Start whichever domain agents exist (`employee-agent` 8083, `attendance-agent` 8084,
   `manager-agent` 8086, ...). Agents not yet built (Payroll, HR, Leave, Budget, CTO) can stay
   down -- routing to them will just report "unavailable" until they exist.
3. Start `orchestrator-agent` on port 8080.
4. Point the React UI's authenticated requests at
   `POST http://localhost:8080/api/v1/orchestrator/chat` with the JWT from the Auth Service.

## Verification

Written to mirror the existing agents' Maven/Spring conventions exactly. Not compiled in this
environment -- Maven Central and the private Google ADK / A2A SDK repositories are not reachable
here -- so please run `mvn -pl orchestrator-agent -am compile` locally before wiring it in.
