package com.shrija.hr.a2a;

import com.shrija.hr.config.HrAiProperties;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class BudgetAgentClient {

  private final HrAiProperties properties;
  private final A2AAgentClientSupport support;

  public BudgetAgentClient(HrAiProperties properties, A2AAgentClientSupport support) {
    this.properties = properties;
    this.support = support;
  }

  public Map<String, Object> sendHeadcountCost(
      String department, int headcount, double estimatedCost) {
    String response =
        support.call(
            properties.budgetAgentUrl(),
            "HR Agent is providing confirmed headcount and estimated cost for department "
                + department
                + ". Confirmed headcount: "
                + headcount
                + ", estimated cost: "
                + estimatedCost
                + ". Use this for department budget planning only.");
    return Map.of(
        "sent",
        true,
        "department",
        department,
        "headcount",
        headcount,
        "budgetAgentResponse",
        response);
  }
}
