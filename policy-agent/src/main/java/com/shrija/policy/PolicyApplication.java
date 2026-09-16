package com.shrija.policy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PolicyApplication {
  public static void main(String[] args) {
    SpringApplication.run(PolicyApplication.class, args);
  }
}
