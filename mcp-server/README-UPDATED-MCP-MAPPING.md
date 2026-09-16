# HRMS MCP Server — Updated Tool Mapping

One Spring AI MCP server exposes **all** tools over a single SSE endpoint. Every agent
(Employee, Attendance, Manager, HR, Leave, Payroll, Expense, Asset, Performance, Recruitment,
Audit, Compliance, Workflow, Document, Notification, Policy) connects to the **same** server and
filters the tool pool down to its own role using a client-side `ALLOWED_TOOLS` allow-list in its
`*McpClient.java`. A tool defined once (e.g. `LeaveTools#decideOnLeaveRequest`) can therefore be
shared by more than one agent (Leave Agent *and* Manager Agent) without duplication.

```
com.hrms.mcpserver.tools
├── EmployeeTools        → employee profile/master-data functions only
├── AttendanceTools      → check-in/out, attendance history, overtime, team attendance
├── ManagerTools         → team roster, approvals, escalation, team-level rollups   (NEW)
├── HrTools              → onboarding, promotion, transfer, exit lifecycle events
├── LeaveTools           → applyForLeave, decideOnLeaveRequest, getLeaveBalances, ...
├── PayrollTools         → calculateGrossSalary, calculateNetSalary, calculateTax, ...
├── ExpenseTools         → submitExpense, approveExpense, calculateReimbursement, ...
├── AssetTools           → assignAsset, returnAsset, getAssetHistory, ...
├── PerformanceTools     → createGoal, getGoals, submitSelfReview/ManagerReview, ...
├── RecruitmentTools     → createJobOpening, createCandidate, scheduleInterview, ...
├── AuditTools           → recordAuditEvent, searchAuditEvents, ...
├── ComplianceTools      → validateAccess, checkSensitiveOperation, ...
├── WorkflowTools        → createWorkflow, advanceWorkflow, approveWorkflow, ...
├── DocumentTools        → createDocument, generatePdf/Docx, storeDocument
├── NotificationTools    → sendEmail, send*Notification (per-domain)
└── PolicyTools          → listPolicies, getPolicy
```

All 16 classes are registered as `@Component` beans and wired into one
`MethodToolCallbackProvider` in `McpToolConfig` — nothing else needs to change when adding a tool
class; just add it to the constructor/`toolObjects(...)` list.

## What changed in this pass

Every agent's `*McpClient.ALLOWED_TOOLS` list was compared against the actual `@Tool` methods on
the server. Three kinds of gaps were found and fixed:

### 1. Missing class: `ManagerTools`
The Manager Agent's prompt and `ManagerMcpClient` already expected 13 tools
(`getTeamMembers`, `getTeamMemberProfile`, `getPendingApprovals`, `getTeamAttendanceReport`,
`escalateToHr`, `getTeamOverview`, `getTeamLeaveSummary`, `getTeamAttendanceSummary`,
`getTeamHeadcount`, `getTeamAlerts`, `getPendingActions`, `getTeamPayrollSummary`,
`getTeamPerformanceSummary`), but no `ManagerTools` class existed on the server. Added it — it
composes `EmployeeRepository` + `AttendanceTools` + `LeaveTools` + `PayrollTools` +
`PerformanceTools` + `NotificationTools` rather than owning its own data, since "manager" is a
role/lens over other domains, not its own data domain. `decideOnLeaveRequest` itself is **not**
duplicated here — the Manager Agent is allow-listed to call the existing `LeaveTools` method
directly.

### 2. Data-type bug: `Employee.managerEmployeeId`
The `Employee` entity declared `managerEmployeeId` as a `String`, but `EmployeeRepository`
declared `findByManagerEmployeeId(Long ...)` and both `AttendanceTools#getTeamAttendance` and
`LeaveTools#getTeamLeaveCalendar` already called it with a `Long`. This mismatch would fail at
Spring Data query-derivation time. Changed the field (and `schema.sql`) to `Long` and simplified
`EmployeeTools` (`getManager`, `getReportingHierarchy`, `updateEmployeeDetails`) to drop the old
`Long.valueOf(...)` string conversions. This was also a prerequisite for `ManagerTools` to work at
all.

### 3. Name/shape mismatches between agents and the server
Several agents were already coded against tool names the server didn't implement (or implemented
under a different name/shape). Reconciled by adding or renaming methods to match what the agents
already call:

| Class | Added | Renamed |
|---|---|---|
| AttendanceTools | `getOvertime` | — |
| AssetTools | `getAssetHistory`, `getAvailableAssets`, `markAssetLost`, `generateAssetReport` | — |
| ExpenseTools | `getExpenseHistory`, `generateExpenseReport`, `sendExpenseToPayroll` | — |
| NotificationTools | `sendPayrollNotification`, `sendLeaveNotification`, `sendDocumentNotification` | — |
| PerformanceTools | `completeGoal`, `submitSelfReview`, `submitManagerReview`, `generatePerformanceSummary` | `createPerformanceGoal`→`createGoal`, `listPerformanceGoals`→`getGoals`, `updatePerformanceGoal`→`updateGoal` |
| RecruitmentTools | `updateJobOpening`, `closeJobOpening`, `searchCandidates`, `shortlistCandidate`, `rejectCandidate`, `getInterviewSchedule`, `moveCandidateStage` | `createJobRequisition`→`createJobOpening`, `createOffer`→`generateOffer` |
| AuditTools | Rebuilt around an audit **event log** (`recordAuditEvent`, `getAuditHistory`, `searchAuditEvents`, `getEmployeeAuditHistory`, `getAgentAuditHistory`, `getSensitiveOperations`) instead of the old audit/finding engagement model, matching what the Audit Agent actually calls | — |
| ComplianceTools | Rebuilt around read-only guardrail checks (`validateAccess`, `checkPolicyCompliance`, `checkSensitiveOperation`, `validateDataExposure`, `checkDocumentCompliance`) instead of the old policy/incident-tracking model, matching what the Compliance Agent actually calls | — |
| WorkflowTools | `advanceWorkflow`, `approveWorkflow`, `rejectWorkflow`, `getPendingWorkflows` | (`approveWorkflowStep`/`rejectWorkflowStep`/`startWorkflow` collapsed into the renamed methods) |

Classes with **no changes** (already matched their agent's `ALLOWED_TOOLS` exactly):
`EmployeeTools`, `LeaveTools`, `PayrollTools`, `HrTools`, `DocumentTools`, `PolicyTools`.

## Registering a new tool class

1. Create `com.hrms.mcpserver.tools.XyzTools` as a `@Component` with `@Tool`-annotated methods.
2. Add it as a constructor parameter and to `.toolObjects(...)` in `McpToolConfig`.
3. On the agent side, add the exact method names it needs to that agent's
   `*McpClient.ALLOWED_TOOLS` list — no server-side per-agent wiring is required.
