package com.shrija.payroll.a2a;

import com.google.adk.agents.BaseAgent;
import com.shrija.payroll.config.PayrollAiProperties;

public class HrAgentClient
{
    private final PayrollAiProperties properties;
    private final A2AAgentClientSupport support;

    public HrAgentClient(PayrollAiProperties properties, A2AAgentClientSupport support) {
        this.properties = properties;
        this.support = support;
    }

    public BaseAgent connect() {
        return support.connect(properties.hrAgentUrl(), "hr-agent", "Attendance Agent");
    }
}
