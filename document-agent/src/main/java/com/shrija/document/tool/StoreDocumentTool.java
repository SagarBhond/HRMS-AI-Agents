package com.shrija.document.tool;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class StoreDocumentTool {
  public Map<String, Object> storeDocument(String requesterEmployeeId, String requesterRole,
      String employeeId, String documentType, String fileName) {
    return Map.of("success", true, "employeeId", employeeId, "documentType", documentType,
        "fileName", fileName, "message", "Document storage requested successfully");
  }
}
