import com.shrija.recruitment.config.RecruitmentAiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {"com.shrija.recruitment"})
@EnableConfigurationProperties(RecruitmentAiProperties.class)
public class RecruitmentApplication {
  public static void main(String[] args) {
    SpringApplication.run(RecruitmentApplication.class, args);
  }
}
