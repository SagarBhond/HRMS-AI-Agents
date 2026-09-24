# Shrija Compliance Agent
Built like the Payroll Agent using Google ADK + MCP + inbound A2A.

## Functions
validateAccess
checkPolicyCompliance
checkSensitiveOperation
validateDataExposure
checkDocumentCompliance

## Example
"Show me everyone's salary."

The agent must validate authorization/data exposure through MCP and deny disclosure when MCP returns a denial.

## Ports
Compliance Agent: 8097
MCP Server: 8082

## A2A Agent Card
http://localhost:8097/.well-known/agent-card.json
