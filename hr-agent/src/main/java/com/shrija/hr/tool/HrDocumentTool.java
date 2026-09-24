package com.shrija.hr.tool;

import com.shrija.hr.a2a.DocumentAgentClient;
import com.shrija.hr.service.AuthorizationService;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class HrDocumentTool {
  private final DocumentAgentClient client;
  private final AuthorizationService auth;

  public HrDocumentTool(DocumentAgentClient client, AuthorizationService auth) {
    this.client = client;
    this.auth = auth;
  }

  public Map<String, Object> createJoiningLetter(String r, String role, Long id, String d) {
    return create("Joining Letter", r, role, id, d);
  }

  public Map<String, Object> createOfferLetter(String r, String role, Long id, String d) {
    return create("Offer Letter", r, role, id, d);
  }

  public Map<String, Object> createPromotionLetter(String r, String role, Long id, String d) {
    return create("Promotion Letter", r, role, id, d);
  }

  public Map<String, Object> createTransferLetter(String r, String role, Long id, String d) {
    return create("Transfer Letter", r, role, id, d);
  }

  public Map<String, Object> createExperienceLetter(String r, String role, Long id, String d) {
    return create("Experience Letter", r, role, id, d);
  }

  public Map<String, Object> createRelievingLetter(String r, String role, Long id, String d) {
    return create("Relieving Letter", r, role, id, d);
  }

  public Map<String, Object> createExitLetter(String r, String role, Long id, String d) {
    return create("Exit Letter", r, role, id, d);
  }

  public Map<String, Object> createSalaryRevisionLetter(String r, String role, Long id, String d) {
    return create("Salary Revision Letter", r, role, id, d);
  }

  public Map<String, Object> createWarningLetter(String r, String role, Long id, String d) {
    return create("Warning Letter", r, role, id, d);
  }

  public Map<String, Object> createAppreciationLetter(String r, String role, Long id, String d) {
    return create("Appreciation Letter", r, role, id, d);
  }

  private Map<String, Object> create(String type, String r, String role, Long id, String details) {
    auth.requirePrivileged(r, role);
    if (id == null) throw new IllegalArgumentException("Employee id is required.");
    return client.createHrDocument(type, r, role, id, details);
  }
}
