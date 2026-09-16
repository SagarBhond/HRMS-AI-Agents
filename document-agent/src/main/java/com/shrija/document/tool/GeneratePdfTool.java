package com.shrija.document.tool;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GeneratePdfTool {
  public Map<String, Object> generatePdf(String requesterEmployeeId, String requesterRole,
      String employeeId, String documentType, String documentContent) {
    String fileName = safe(documentType) + "_" + employeeId + ".pdf";
    return Map.of("success", true, "employeeId", employeeId, "documentType", documentType,
        "format", "PDF", "fileName", fileName, "message", "PDF generation requested successfully");
  }
  private String safe(String value) { return value.toLowerCase().replaceAll("[^a-z0-9]+", "_"); }
}
