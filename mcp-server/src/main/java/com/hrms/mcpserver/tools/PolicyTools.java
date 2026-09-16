package com.hrms.mcpserver.tools;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class PolicyTools {
  private final AtomicLong ids = new AtomicLong(1);
  private final Map<Long, PolicyRecord> policies = new ConcurrentHashMap<>();

  @Tool(description = "Create an HR policy")
  public PolicyRecord createPolicy(String name, String description, String category) {
    long id = ids.getAndIncrement();
    PolicyRecord p = new PolicyRecord(id, name, description, category, "ACTIVE", Instant.now().toString());
    policies.put(id, p); return p;
  }

  @Tool(description = "Update an HR policy")
  public PolicyRecord updatePolicy(Long policyId, String name, String description, String category, String status) {
    PolicyRecord old = getPolicy(policyId);
    PolicyRecord p = new PolicyRecord(policyId, name == null ? old.name() : name,
        description == null ? old.description() : description,
        category == null ? old.category() : category,
        status == null ? old.status() : status, old.createdAt());
    policies.put(policyId, p); return p;
  }

  @Tool(description = "Get an HR policy")
  public PolicyRecord getPolicy(Long policyId) {
    PolicyRecord p = policies.get(policyId);
    if (p == null) throw new IllegalArgumentException("No policy found with id " + policyId);
    return p;
  }

  @Tool(description = "List HR policies")
  public List<PolicyRecord> listPolicies() { return policies.values().stream().toList(); }

  @Tool(description = "Validate an HR policy")
  public ValidationResult validatePolicy(Long policyId) {
    PolicyRecord p = getPolicy(policyId);
    return new ValidationResult(policyId, p.name() != null && !p.name().isBlank() && p.description() != null && !p.description().isBlank(), "Policy contains required fields");
  }

  @Tool(description = "Check policy eligibility for an employee")
  public EligibilityResult checkPolicyEligibility(Long employeeId, Long policyId) {
    getPolicy(policyId);
    return new EligibilityResult(employeeId, policyId, true, "Eligibility requires no additional rules in the current policy model");
  }

  public record PolicyRecord(Long policyId, String name, String description, String category, String status, String createdAt) {}
  public record ValidationResult(Long policyId, boolean valid, String message) {}
  public record EligibilityResult(Long employeeId, Long policyId, boolean eligible, String message) {}
}
