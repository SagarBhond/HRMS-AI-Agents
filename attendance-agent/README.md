# Attendance Agent

Google ADK Attendance Agent using MCP for all attendance domain tools and A2A for cross-agent dependencies.

## MCP

The agent connects to the MCP server at `http://localhost:8082` through `attendanceMcpToolset`.
All attendance capabilities are exposed to the ADK agent with:

```java
.tools(ImmutableList.of(attendanceMcpToolset))
```

No separate `tool` package is used.

## Attendance MCP tools

- checkIn
- checkOut
- getTodayAttendance
- getAttendanceHistory
- getMonthlyAttendance
- getAttendanceSummary
- getWorkingHours
- getOvertime
- getTeamAttendance
- calculateAttendancePercentage
- getLateArrivals
- getEarlyDepartures
- getAbsenceSummary
- getAttendanceTrend
- generateAttendanceReport
- generateTeamAttendanceReport
- detectAttendanceIssues

## Port

Attendance Agent: `8084`
MCP Server: `8082`
