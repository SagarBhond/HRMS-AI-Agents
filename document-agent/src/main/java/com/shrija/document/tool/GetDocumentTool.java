package com.shrija.document.tool;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GetDocumentTool {
  public Map<String, Object> getDocument(String requesterEmployeeId, String requesterRole,
      String employeeId, String documentId) {
    if (documentId == null || documentId.isBlank()) return Map.of("success", false, "message", "documentId is required");
    return Map.of("success", true, "employeeId", employeeId, "documentId", documentId,
        "message", "Document lookup requested successfully");
  }
}
