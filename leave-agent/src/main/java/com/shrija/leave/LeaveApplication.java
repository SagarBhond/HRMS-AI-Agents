package com.shrija.leave;

import com.shrija.leave.config.LeaveAiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(LeaveAiProperties.class)
public class LeaveApplication {

  public static void main(String[] args) {
    SpringApplication.run(LeaveApplication.class, args);
  }
}
