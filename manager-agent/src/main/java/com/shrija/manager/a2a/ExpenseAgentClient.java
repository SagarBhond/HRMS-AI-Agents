package com.shrija.manager.a2a;
import com.shrija.manager.config.ManagerAiProperties;
import org.springframework.stereotype.Component;
@Component
public class ExpenseAgentClient {
  private final ManagerAiProperties properties; private final A2AAgentClientSupport support;
  public ExpenseAgentClient(ManagerAiProperties properties, A2AAgentClientSupport support) {
    this.properties=properties; this.support=support;
  }
  public String getTeamExpenseSummary(String managerEmployeeId) {
    return support.call(properties.expenseAgentUrl(),
        "Manager " + managerEmployeeId + " requests confirmed team expense information. Return only confirmed data.");
  }
}
