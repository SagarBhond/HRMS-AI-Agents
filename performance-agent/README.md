# Shrija Performance Agent

Google ADK + MCP + inbound A2A Performance Agent, structured like the supplied Payroll Agent.

## Functions
- createGoal
- updateGoal
- getGoals
- completeGoal
- submitSelfReview
- submitManagerReview
- createPerformanceReview
- getPerformanceReview
- generatePerformanceSummary

## Tool classes
- GoalTool
- PerformanceReviewTool
- ManagerReviewTool
- PerformanceReportTool

Execution is through the shared MCP server; the Tool classes document capability groupings and do not duplicate MCP business logic.

## Ports
Performance Agent: 8094
MCP Server: 8082

## REST
POST /api/v1/performance/chat

Example:
{"userId":"25","role":"EMPLOYEE","message":"Show my performance goals."}

## A2A Agent Card
http://localhost:8094/.well-known/agent-card.json

The MCP server must expose the nine functions with these exact names. The agent never invents goals, reviews, ratings, IDs, dates or summaries.
