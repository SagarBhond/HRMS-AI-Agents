package com.shrija.document.tool;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ListDocumentsTool {
  public Map<String, Object> listDocuments(String requesterEmployeeId, String requesterRole, String employeeId) {
    return Map.of("success", true, "employeeId", employeeId, "documents", List.of());
  }
}
