package com.shrija.hr.a2a;
import com.shrija.hr.config.HrAiProperties;
import com.shrija.hr.config.HrAiProperties;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class RecruitmentAgentClient
{
    private final HrAiProperties properties;
    private final A2AAgentClientSupport support;

    public RecruitmentAgentClient(
            HrAiProperties properties,
            A2AAgentClientSupport support) {
        this.properties = properties;
        this.support = support;
    }

    public Map<String, Object> getRecruitmentStatus(String employeeId) {

        String response =
                support.call(
                        properties.recruitmentAgentUrl(),
                        "Leave Agent needs recruitment information for employee "
                                + employeeId
                                + ". Retrieve the relevant recruitment/onboarding status "
                                + "and confirm whether the employee's joining status is complete. "
                                + "Do not modify recruitment records.");

        return Map.of(
                "sent", true,
                "employeeId", employeeId,
                "recruitmentAgentResponse", response);
    }
}
