package com.shrija.document.a2a;

import com.google.adk.agents.BaseAgent;
import com.shrija.document.config.DocumentAiProperties;

public class RecruitmentAgentClient
{
    private final DocumentAiProperties properties;
    private final A2AAgentClientSupport support;
    public RecruitmentAgentClient(DocumentAiProperties properties, A2AAgentClientSupport support) {
        this.properties = properties; this.support = support;
    }
    public BaseAgent connect() {
        return support.connect(properties.recruitmentAgentUrl(), "recruitment-agent", "Recruitment Agent");
    }
}
