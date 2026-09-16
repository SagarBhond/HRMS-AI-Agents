package com.shrija.hr.tool;

import com.shrija.hr.a2a.BudgetAgentClient;
import com.shrija.hr.service.AuthorizationService;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class HrBudgetTool {

  private final BudgetAgentClient budgetAgentClient;
  private final AuthorizationService authorizationService;

  public HrBudgetTool(BudgetAgentClient budgetAgentClient, AuthorizationService authorizationService) {
    this.budgetAgentClient = budgetAgentClient;
    this.authorizationService = authorizationService;
  }

  public Map<String, Object> sendHeadcountCostToBudget(
      String requesterEmployeeId,
      String requesterRole,
      String department,
      int headcount,
      double estimatedCost) {
    authorizationService.requirePrivileged(requesterEmployeeId, requesterRole);
    return budgetAgentClient.sendHeadcountCost(department, headcount, estimatedCost);
  }
}
