package com.shrija.hr.a2a;

import com.shrija.hr.config.HrAiProperties;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * HR Agent-side client for sending messages back to the Orchestrator Agent over A2A —
 * e.g. escalating a request the HR Agent cannot fully resolve on its own, or reporting
 * a status update that the Orchestrator needs to relay to the end user.
 */
@Component
public class OrchestrationAgentClient {

    private final HrAiProperties properties;
    private final A2AAgentClientSupport support;

    public OrchestrationAgentClient(HrAiProperties properties, A2AAgentClientSupport support) {
        this.properties = properties;
        this.support = support;
    }

    public Map<String, Object> sendToOrchestrator(String message) {
        String response = support.call(properties.orchestrationAgentUrl(), message);
        return Map.of("sent", true, "orchestratorResponse", response);
    }
    }
