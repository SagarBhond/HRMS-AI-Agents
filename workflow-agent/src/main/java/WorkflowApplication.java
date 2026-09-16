import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.shrija.workflow.config.WorkflowAiProperties;

@SpringBootApplication(scanBasePackages={"com.shrija.workflow"})
@EnableConfigurationProperties(WorkflowAiProperties.class)
public class WorkflowApplication { public static void main(String[] args) { SpringApplication.run(WorkflowApplication.class,args); } }
