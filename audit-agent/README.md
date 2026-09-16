# Shrija Audit Agent

Audit Agent built like the Payroll Agent using Google ADK + MCP + inbound A2A.

## Package
`com.shrija.audit.agent`

## Responsibilities

The Audit Agent is the audit trail service. It records and retrieves confirmed audit evidence and does not directly modify business-domain data.

## MCP functions

- `recordAuditEvent`
- `getAuditHistory`
- `searchAuditEvents`
- `getEmployeeAuditHistory`
- `getAgentAuditHistory`
- `getSensitiveOperations`

Only these six functions are exposed to the agent's MCP toolset.

## Audit event

An audit event can contain:

- Actor
- Role
- Action
- Target Employee
- Timestamp
- Agent
- Tool
- Old Value
- New Value
- Status

## Example

User:
`Who changed employee 25's designation?`

Flow:

```text
User / Orchestrator
        |
       A2A
        ↓
  Audit Agent :8096
        |
       MCP
        ↓
 Search audit records
        |
        ↓
 Return confirmed event
```

## REST

`POST /api/v1/audit/chat`

Example:

```json
{
  "userId": "25",
  "role": "HR",
  "message": "Who changed employee 25's designation?"
}
```

## A2A Agent Card

`http://localhost:8096/.well-known/agent-card.json`

## Ports

- Audit Agent: `8096`
- MCP Server: `8082`

## Important

The Audit Agent never invents audit records. The shared MCP server must expose the six functions above with these exact names. Audit data should be recorded by the domain operations/integrations that perform the underlying business action.
