import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.shrija.compliance.config.ComplianceAiProperties;

@SpringBootApplication(scanBasePackages={"com.shrija.compliance"})
@EnableConfigurationProperties(ComplianceAiProperties.class)
public class ComplianceApplication { public static void main(String[] args) { SpringApplication.run(ComplianceApplication.class,args); } }
