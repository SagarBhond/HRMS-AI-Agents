package com.shrija.hr.a2a;

import com.shrija.hr.config.HrAiProperties;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DocumentAgentClient {
  private final HrAiProperties properties;
  private final A2AAgentClientSupport support;

  public DocumentAgentClient(HrAiProperties properties, A2AAgentClientSupport support) {
    this.properties = properties;
    this.support = support;
  }

  public Map<String, Object> createHrDocument(
      String documentType,
      String requesterEmployeeId,
      String requesterRole,
      Long employeeId,
      String details) {
    if (properties.documentAgentUrl() == null || properties.documentAgentUrl().isBlank())
      throw new IllegalStateException("Document Agent A2A URL is not configured.");
    String prompt =
        "Create an HR document using the Document Agent. Document type: "
            + documentType
            + ". Employee ID: "
            + employeeId
            + ". Requested by employee ID: "
            + requesterEmployeeId
            + ", role: "
            + requesterRole
            + ". Details: "
            + (details == null ? "" : details)
            + ". Do not invent employee data. Return the document creation result and identifier.";
    String response = support.call(properties.documentAgentUrl(), prompt);
    return Map.of(
        "created",
        true,
        "documentType",
        documentType,
        "employeeId",
        employeeId,
        "documentAgentResponse",
        response);
  }
}
