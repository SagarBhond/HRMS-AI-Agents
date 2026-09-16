package com.shrija.document.tool;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GenerateDocxTool {
  public Map<String, Object> generateDocx(String requesterEmployeeId, String requesterRole,
      String employeeId, String documentType, String documentContent) {
    String fileName = safe(documentType) + "_" + employeeId + ".docx";
    return Map.of("success", true, "employeeId", employeeId, "documentType", documentType,
        "format", "DOCX", "fileName", fileName, "message", "DOCX generation requested successfully");
  }
  private String safe(String value) { return value.toLowerCase().replaceAll("[^a-z0-9]+", "_"); }
}
