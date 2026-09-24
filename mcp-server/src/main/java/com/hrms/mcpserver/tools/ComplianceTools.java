package com.hrms.mcpserver.tools;

import java.util.List;
import java.util.Set;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Tools backing the Compliance Agent — read-only guardrail checks consulted by other agents before
 * exposing sensitive data or performing sensitive operations. Does not own its own business
 * records; it evaluates requests against a small set of configured rules.
 */
@Component
public class ComplianceTools {

  /** Actions that are always treated as sensitive regardless of role. */
  private static final Set<String> SENSITIVE_ACTIONS =
      Set.of("VIEW_SALARY", "RECORD_EXIT", "VIEW_PII", "EXPORT_DATA", "DELETE_RECORD");

  /** Fields that must never be exposed outside of the owning employee or HR. */
  private static final Set<String> RESTRICTED_FIELDS =
      Set.of("salary", "bankAccount", "ssn", "taxId", "medicalRecord");

  @Tool(
      description =
          "Validate whether a requester role may access a resource belonging to a given employee")
  public AccessDecision validateAccess(
      @ToolParam(description = "Role of the requester: EMPLOYEE, MANAGER, HR, ADMIN")
          String requesterRole,
      @ToolParam(description = "Employee ID that owns the resource") Long resourceOwnerEmployeeId,
      @ToolParam(description = "Employee ID of the requester") Long requesterEmployeeId,
      @ToolParam(description = "Resource being accessed, e.g. PROFILE, SALARY, LEAVE")
          String resource) {
    boolean isSelf =
        requesterEmployeeId != null && requesterEmployeeId.equals(resourceOwnerEmployeeId);
    boolean isPrivileged =
        requesterRole != null
            && (requesterRole.equalsIgnoreCase("HR") || requesterRole.equalsIgnoreCase("ADMIN"));
    boolean isManagerResource = requesterRole != null && requesterRole.equalsIgnoreCase("MANAGER");
    boolean allowed = isSelf || isPrivileged || isManagerResource;
    String reason =
        allowed
            ? "Access permitted for role " + requesterRole
            : "Role " + requesterRole + " may not access another employee's " + resource;
    return new AccessDecision(allowed, reason);
  }

  @Tool(description = "Check whether a proposed action complies with a named HR policy")
  public ComplianceCheckResult checkPolicyCompliance(
      @ToolParam(description = "Policy name, e.g. LEAVE_POLICY, EXPENSE_POLICY") String policyName,
      @ToolParam(description = "Action being evaluated") String action,
      @ToolParam(description = "Free-text context for the check") String context) {
    boolean compliant =
        policyName != null && !policyName.isBlank() && action != null && !action.isBlank();
    return new ComplianceCheckResult(
        compliant,
        compliant
            ? "Action satisfies " + policyName
            : "Missing policy name or action for compliance check");
  }

  @Tool(description = "Check whether an action is classified as a sensitive operation")
  public SensitivityResult checkSensitiveOperation(
      @ToolParam(description = "Action name") String action) {
    boolean sensitive = action != null && SENSITIVE_ACTIONS.contains(action.toUpperCase());
    return new SensitivityResult(
        action,
        sensitive,
        sensitive
            ? "Action requires elevated authorization and audit logging"
            : "Action is not flagged as sensitive");
  }

  @Tool(
      description = "Validate whether returning a given set of fields would expose restricted data")
  public DataExposureResult validateDataExposure(
      @ToolParam(description = "Comma-separated field names about to be exposed") String fields) {
    List<String> requested =
        fields == null
            ? List.of()
            : java.util.Arrays.stream(fields.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    List<String> flagged =
        requested.stream().filter(f -> RESTRICTED_FIELDS.contains(f.toLowerCase())).toList();
    return new DataExposureResult(flagged.isEmpty(), flagged);
  }

  @Tool(
      description =
          "Check whether a document type is subject to compliance retention/handling rules")
  public DocumentComplianceResult checkDocumentCompliance(
      @ToolParam(description = "Document type, e.g. CONTRACT, PAYSLIP, ID_PROOF")
          String documentType) {
    boolean regulated =
        documentType != null
            && (documentType.equalsIgnoreCase("CONTRACT")
                || documentType.equalsIgnoreCase("PAYSLIP")
                || documentType.equalsIgnoreCase("ID_PROOF")
                || documentType.equalsIgnoreCase("MEDICAL_RECORD"));
    return new DocumentComplianceResult(
        documentType,
        regulated,
        regulated
            ? "Document type is regulated; apply retention and access-control rules"
            : "No special compliance handling required for this document type");
  }

  public record AccessDecision(boolean allowed, String reason) {}

  public record ComplianceCheckResult(boolean compliant, String message) {}

  public record SensitivityResult(String action, boolean sensitive, String message) {}

  public record DataExposureResult(boolean safe, List<String> restrictedFieldsFound) {}

  public record DocumentComplianceResult(String documentType, boolean regulated, String message) {}
}
