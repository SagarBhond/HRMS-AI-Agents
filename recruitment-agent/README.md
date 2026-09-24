# Shrija Recruitment Agent

Recruitment Agent implemented in the same Google ADK + MCP + inbound A2A style as the supplied Payroll Agent.

## Package
`com.shrija.recruitment.agent`

## Tool classes
- `JobOpeningTool`
- `CandidateTool`
- `InterviewTool`
- `RecruitmentPipelineTool`
- `OfferTool`

## MCP functions
- `createJobOpening`
- `updateJobOpening`
- `closeJobOpening`
- `createCandidate`
- `searchCandidates`
- `shortlistCandidate`
- `rejectCandidate`
- `scheduleInterview`
- `getInterviewSchedule`
- `moveCandidateStage`
- `generateOffer`

## Workflow
JOB -> CANDIDATE -> SCREENING -> SHORTLIST -> INTERVIEW -> SELECTED -> OFFER -> ONBOARDING

## Architecture
User/Orchestrator -> A2A -> Recruitment Agent :8095 -> MCP -> Shared MCP Server :8082

The Tool classes are capability groupings; actual execution remains in the shared MCP server through `McpToolset`, so recruitment business logic is not duplicated inside the agent.

## A2A Agent Card
`http://localhost:8095/.well-known/agent-card.json`

## REST
`POST /api/v1/recruitment/chat`

Example:
```json
{
  "message": "Show open job openings.",
  "userId": "25",
  "role": "HR"
}
```

The MCP server must expose the 11 functions above with these exact names.
