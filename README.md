# HRMS Shared MCP Server

This version connects the current specialist agents to one shared MCP server on port `8082`.

## Architecture

`Agent -> MCP Server :8082 -> Domain Tool -> Data`

A2A remains responsible for `Agent <-> Agent` communication. The Orchestrator is an A2A router and does not need the MCP toolset merely to route requests.

## Agent -> MCP mapping

- Employee :8083 -> Employee tools
- Attendance :8084 -> Attendance tools
- Manager :8086 -> Manager tools
- HR :8088 -> Employee + HR lifecycle tools
- Leave :8087 -> Leave tools
- Payroll :8085 -> Payroll tools
- Document :8089 -> Document tools
- Notification :8090 -> Notification tools
- Policy :8091 -> Policy tools
- Expense :8092 -> Expense tools
- Asset :8093 -> Asset tools
- Performance :8094 -> Performance tools
- Recruitment :8095 -> Recruitment tools
- Audit :8096 -> Audit tools
- Compliance :8097 -> Compliance tools
- Workflow :8098 -> Workflow tools

## Important exact tool names

The MCP server now exposes the exact names expected by the current agents, including:

### Performance
`createGoal`, `updateGoal`, `getGoals`, `completeGoal`, `submitSelfReview`, `submitManagerReview`, `createPerformanceReview`, `getPerformanceReview`, `generatePerformanceSummary`

### Recruitment
`createJobOpening`, `updateJobOpening`, `closeJobOpening`, `createCandidate`, `searchCandidates`, `shortlistCandidate`, `rejectCandidate`, `scheduleInterview`, `getInterviewSchedule`, `moveCandidateStage`, `generateOffer`

### Audit
`recordAuditEvent`, `getAuditHistory`, `searchAuditEvents`, `getEmployeeAuditHistory`, `getAgentAuditHistory`, `getSensitiveOperations`

### Compliance
`validateAccess`, `checkPolicyCompliance`, `checkSensitiveOperation`, `validateDataExposure`, `checkDocumentCompliance`

### Workflow
`createWorkflow`, `getWorkflow`, `advanceWorkflow`, `approveWorkflow`, `rejectWorkflow`, `cancelWorkflow`, `getPendingWorkflows`

### Asset
`assignAsset`, `returnAsset`, `getEmployeeAssets`, `getAssetHistory`, `getAvailableAssets`, `markAssetLost`, `generateAssetReport`

### Expense
`submitExpense`, `getExpense`, `getExpenseHistory`, `approveExpense`, `rejectExpense`, `calculateReimbursement`, `generateExpenseReport`, `sendExpenseToPayroll`

### Notification
`sendEmail`, `sendEmployeeNotification`, `sendManagerNotification`, `sendHrNotification`, `sendPayrollNotification`, `sendLeaveNotification`, `sendDocumentNotification`

## MCP URL

The current agents use:

`http://localhost:8082`

The server is configured for SSE transport with message endpoint `/mcp/message`.

## Start order

1. Start the MCP server on `8082`.
2. Start specialist agents.
3. Verify each agent can discover its allowlisted tools.
4. Start the Orchestrator and test A2A routing.

## Note on storage

The original server contains JPA/MySQL-backed Employee, Attendance, Leave, Payroll and HR data. Several newer domain tools in the supplied project are intentionally lightweight in-memory stores (Asset, Performance, Recruitment, Audit, Compliance, Workflow, Notification, Expense, Policy, Document). Those stores reset when the MCP server restarts. Replace those stores with JPA repositories when persistence is required.
