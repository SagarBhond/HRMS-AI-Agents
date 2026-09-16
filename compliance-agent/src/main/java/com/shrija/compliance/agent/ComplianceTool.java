package com.shrija.compliance.agent;
/** Documents the compliance capability boundary. Actual execution is provided by the shared MCP server. */
public final class ComplianceTool {
 private ComplianceTool(){}
 public static final String[] FUNCTIONS={"validateAccess","checkPolicyCompliance","checkSensitiveOperation","validateDataExposure","checkDocumentCompliance"};
}
