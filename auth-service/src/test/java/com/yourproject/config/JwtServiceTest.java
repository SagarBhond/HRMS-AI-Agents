package com.yourproject.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.yourproject.entity.Role;
import com.yourproject.entity.User;
import com.yourproject.service.InvalidCredentialsException;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  private static final String SECRET = "MyLocalHRMSJwtSecretKey2026SecureValue123456";

  @Test
  void tokenContainsTheIdentityContractUsedByTheOrchestrator() {
    JwtService service = new JwtService(SECRET, 60_000);
    User user = new User("employee@example.com", "hash", Role.EMPLOYEE, 73L);
    user.setId(9L);

    JwtService.AuthenticatedUser authenticated =
        service.authenticate("Bearer " + service.generateToken(user));

    assertThat(authenticated.userId()).isEqualTo(9L);
    assertThat(authenticated.username()).isEqualTo("employee@example.com");
    assertThat(authenticated.role()).isEqualTo(Role.EMPLOYEE);
    assertThat(authenticated.employeeId()).isEqualTo(73L);
  }

  @Test
  void bootstrapAdminCanHaveNoEmployeeProfile() {
    JwtService service = new JwtService(SECRET, 60_000);
    User user = new User("admin@example.com", "hash", Role.ADMIN, null);
    user.setId(1L);

    JwtService.AuthenticatedUser authenticated =
        service.authenticate("Bearer " + service.generateToken(user));

    assertThat(authenticated.employeeId()).isNull();
  }

  @Test
  void tamperedTokenIsRejected() {
    JwtService service = new JwtService(SECRET, 60_000);
    User user = new User("employee@example.com", "hash", Role.EMPLOYEE, 73L);
    user.setId(9L);
    String token = service.generateToken(user);

    assertThatThrownBy(() -> service.authenticate("Bearer " + token + "broken"))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Invalid or expired token");
  }
}
