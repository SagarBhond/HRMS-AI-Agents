# Shrija Workflow Agent
Google ADK + MCP + inbound A2A business-process layer.

## Exact functions
createWorkflow
getWorkflow
advanceWorkflow
approveWorkflow
rejectWorkflow
cancelWorkflow
getPendingWorkflows

## Business workflows
Onboarding: HR -> Employee -> Document -> Payroll -> Manager -> Notification
Offboarding: HR -> Manager -> Asset -> Leave -> Payroll -> Document -> Notification

The Workflow Agent owns workflow state and transitions. Domain data remains owned by the specialized agents/services and should be reached through A2A.

## Ports
Workflow Agent: 8098
MCP Server: 8082

## A2A Agent Card
http://localhost:8098/.well-known/agent-card.json
