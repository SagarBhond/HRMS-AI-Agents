package com.shrija.attendance;

import com.shrija.attendance.config.AttendanceAiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AttendanceAiProperties.class)
public class AttendanceApplication {

  public static void main(String[] args) {
    SpringApplication.run(AttendanceApplication.class, args);
  }
}
