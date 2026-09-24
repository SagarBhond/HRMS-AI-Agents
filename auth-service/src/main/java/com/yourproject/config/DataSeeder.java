package com.yourproject.config;

import com.yourproject.entity.Role;
import com.yourproject.entity.User;
import com.yourproject.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates the single bootstrap administrator used to provision every other account. */
@Component
public class DataSeeder implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final boolean enabled;
  private final String username;
  private final String password;

  public DataSeeder(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      @Value("${auth.seed-admin.enabled:true}") boolean enabled,
      @Value("${auth.seed-admin.username}") String username,
      @Value("${auth.seed-admin.password}") String password) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.enabled = enabled;
    this.username = username;
    this.password = password;
  }

  @Override
  public void run(String... args) {
    if (!enabled) {
      return;
    }
    if (username == null || username.isBlank() || password == null || password.length() < 8) {
      throw new IllegalStateException(
          "Bootstrap admin username and password (minimum 8 characters) must be configured");
    }
    String normalizedUsername = username.trim().toLowerCase();
    userRepository
        .findByUsernameIgnoreCase(normalizedUsername)
        .ifPresentOrElse(
            existing -> log.info("Bootstrap administrator already exists: {}", normalizedUsername),
            () -> {
              userRepository.save(
                  new User(normalizedUsername, passwordEncoder.encode(password), Role.ADMIN, null));
              log.info("Created bootstrap administrator: {}", normalizedUsername);
            });
  }
}
