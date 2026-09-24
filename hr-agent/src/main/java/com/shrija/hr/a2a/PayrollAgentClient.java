package com.shrija.hr.a2a;

import com.shrija.hr.config.HrAiProperties;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PayrollAgentClient {

  private final HrAiProperties properties;
  private final A2AAgentClientSupport support;

  public PayrollAgentClient(HrAiProperties properties, A2AAgentClientSupport support) {
    this.properties = properties;
    this.support = support;
  }

  public Map<String, Object> triggerFinalSettlement(
      String employeeId,
      String eventDate,
      double finalBasicSalary,
      double finalAllowances,
      double perDayRate) {
    String response =
        support.call(
            properties.payrollAgentUrl(),
            "HR Agent is confirming employee "
                + employeeId
                + " exited effective "
                + eventDate
                + ". Confirmed final basic salary "
                + finalBasicSalary
                + ", final allowances "
                + finalAllowances
                + ", per-day rate "
                + perDayRate
                + ". Generate the final settlement salary slip for this exit.");
    return Map.of("sent", true, "employeeId", employeeId, "payrollAgentResponse", response);
  }
}
