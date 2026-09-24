package com.shrija.leave.a2a;

import com.shrija.leave.config.LeaveAiProperties;
import com.shrija.leave.service.AuthorizationService;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DocumentAgentClient {

  private final LeaveAiProperties properties;
  private final A2AAgentClientSupport support;
  private final AuthorizationService authorizationService;

  public DocumentAgentClient(
      LeaveAiProperties properties,
      A2AAgentClientSupport support,
      AuthorizationService authorizationService) {
    this.properties = properties;
    this.support = support;
    this.authorizationService = authorizationService;
  }

  /**
   * Thin FunctionTool bridge. The actual operation is performed by the dedicated A2A agent. The
   * actual document creation is performed by Document Agent over A2A.
   */
  public Map<String, Object> createLeaveDocument(
      String requesterEmployeeId, String requesterRole, String employeeId, String details) {

    authorizationService.requireSelfOrPrivileged(requesterEmployeeId, requesterRole, employeeId);

    String response =
        support.call(
            properties.documentAgentUrl(),
            "Create a leave-related document for employee "
                + employeeId
                + ". Requested by employee "
                + requesterEmployeeId
                + " with role "
                + requesterRole
                + ". Confirmed leave details: "
                + details
                + ". Do not invent leave or employee data. Return the document creation result.");

    return Map.of(
        "created", true,
        "employeeId", employeeId,
        "documentAgentResponse", response);
  }
}
