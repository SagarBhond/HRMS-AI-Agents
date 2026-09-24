package com.shrija.document.tool;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CreateDocumentTool {
  public Map<String, Object> createDocument(String requesterEmployeeId, String requesterRole,
      String employeeId, String documentType, String documentContent) {
    if (employeeId == null || employeeId.isBlank() || documentType == null || documentType.isBlank()) {
      return Map.of("success", false, "message", "employeeId and documentType are required");
    }
    return Map.of("success", true, "employeeId", employeeId, "documentType", documentType,
        "contentPrepared", true, "message", "Document content prepared");
  }
}
