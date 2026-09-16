package com.shrija.hr;

import com.shrija.hr.config.HrAiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(HrAiProperties.class)
public class HrApplication {

  public static void main(String[] args) {
    SpringApplication.run(HrApplication.class, args);
  }
}
