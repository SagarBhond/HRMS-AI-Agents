# Payroll Agent

Standalone Payroll Agent for Shrija HRMS.  
**Architecture is identical to employee-agent and attendance-agent.**

## Runtime

| Item | Value |
|------|--------|
| Java | 21 |
| Port | **8085** |
| MCP | `http://localhost:8082` (SSE) |
| Package | `com.shrija.payroll` |

## Structure (same as employee / attendance)

```
com.shrija.payroll
├── a2a/                  A2A clients + support (Employee, Attendance, Leave, Manager)
├── agent/                LlmAgent (Gemini + FunctionTools)
├── config/               Properties, Gemini, A2A server
├── controller/           POST /api/v1/payroll/chat
├── dto/
├── exception/
├── mcp/                  PayrollMcpClient (SSE + retry) — only path to data
├── service/              ConversationService + AuthorizationService
└── tool/                 FunctionTools → MCP
```

## Data storage — important

This agent **never** uses JDBC / JPA / repositories.

All reads and writes go through the shared MCP server tools defined in:

`com.hrms.mcpserver.tools.PayrollTools`

| MCP tool name           | Purpose                                      |
|-------------------------|----------------------------------------------|
| `generateSalarySlip`    | Create/update salary slip (persists to DB)   |
| `markSalarySlipAsPaid`  | Set status = PAID                            |
| `getSalarySlip`         | Fetch slip for employee + month + year       |

If rows are not appearing in the `salary_slip` table:

1. Confirm MCP server is running on 8082
2. Confirm `PayrollTools` is registered in `McpToolConfig`
3. Check agent logs for MCP call failures
4. Verify MySQL credentials and that `ddl-auto: update` created the table

## API

```http
POST /api/v1/payroll/chat
Content-Type: application/json

{
  "userId": "1",
  "role": "HR",
  "message": "Generate salary slip for employee 1 for August 2026 with basic 50000, allowances 8000 and per day rate 2000"
}
```

```json
{
  "userId": "1",
  "role": "EMPLOYEE",
  "message": "Get my salary slip for August 2026"
}
```

## Environment

| Variable | Default |
|----------|---------|
| `GOOGLE_API_KEY` | (required) |
| `PAYROLL_GEMINI_MODEL` | `gemini-3.1-flash-lite` |
| `PAYROLL_MCP_SERVER_URL` | `http://localhost:8082` |
| `EMPLOYEE_AGENT_A2A_URL` | `http://localhost:8083` |
| `ATTENDANCE_AGENT_A2A_URL` | `http://localhost:8084` |
| `LEAVE_AGENT_A2A_URL` | `http://localhost:8087` |
| `MANAGER_AGENT_A2A_URL` | `http://localhost:8086` |
| `PAYROLL_AGENT_PORT` | `8085` |

## Start order

1. MySQL + MCP server (`mcp-server` on 8082)
2. Employee / Attendance / Leave / Manager agents (for future A2A)
3. Payroll agent on 8085


## Payroll capabilities

The agent exposes these MCP tools through `payrollMcpToolset`:

- generateSalarySlip
- markSalarySlipAsPaid
- getSalarySlip
- calculateGrossSalary
- calculateNetSalary
- calculateTax
- calculateDeductions
- calculateOvertimePay
- calculateLeaveDeduction
- calculateBonus
- calculateReimbursement
- getPayrollHistory
- getPayrollSummary
- generatePayrollReport
- explainSalary

The ADK agent registers the MCP toolset with `.tools(ImmutableList.of(payrollMcpToolset))`, matching the Employee Agent architecture.
