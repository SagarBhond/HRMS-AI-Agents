package com.yourproject.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yourproject.config.JwtService;
import com.yourproject.dto.CreateUserRequest;
import com.yourproject.dto.UserResponse;
import com.yourproject.entity.Role;
import com.yourproject.entity.User;
import com.yourproject.repository.EmployeeAccountRepository;
import com.yourproject.repository.UserRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

  private final UserRepository userRepository = mock(UserRepository.class);
  private final EmployeeAccountRepository employeeRepository =
      mock(EmployeeAccountRepository.class);
  private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
  private final JwtService jwtService = mock(JwtService.class);
  private final AuthService service =
      new AuthService(userRepository, employeeRepository, passwordEncoder, jwtService);

  @ParameterizedTest
  @CsvSource({
    "ADMIN,MANAGER",
    "ADMIN,HR",
    "ADMIN,EMPLOYEE",
    "MANAGER,HR",
    "MANAGER,EMPLOYEE",
    "HR,EMPLOYEE"
  })
  void allowedHierarchyCreatesAndLinksAccount(Role creatorRole, Role requestedRole) {
    User creator = user(1L, "creator@example.com", creatorRole, 10L);
    authenticateAs(creator);
    CreateUserRequest request = request(requestedRole);
    when(userRepository.findByUsernameIgnoreCase(request.username())).thenReturn(Optional.empty());
    when(employeeRepository.findByEmail(request.username())).thenReturn(null);
    when(employeeRepository.create(request)).thenReturn(44L);
    when(passwordEncoder.encode(request.password())).thenReturn("encoded");
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User saved = invocation.getArgument(0);
              saved.setId(2L);
              return saved;
            });

    UserResponse response = service.createUser("Bearer token", request);

    assertThat(response.role()).isEqualTo(requestedRole);
    assertThat(response.employeeId()).isEqualTo(44L);
    verify(employeeRepository).create(request);
  }

  @ParameterizedTest
  @CsvSource({
    "MANAGER,MANAGER",
    "MANAGER,ADMIN",
    "HR,HR",
    "HR,MANAGER",
    "EMPLOYEE,EMPLOYEE",
    "EMPLOYEE,HR"
  })
  void forbiddenHierarchyIsRejected(Role creatorRole, Role requestedRole) {
    User creator = user(1L, "creator@example.com", creatorRole, 10L);
    authenticateAs(creator);

    assertThatThrownBy(() -> service.createUser("Bearer token", request(requestedRole)))
        .isInstanceOf(ForbiddenOperationException.class);
  }

  @Test
  void existingEmployeeProfileIsLinkedInsteadOfDuplicated() {
    User creator = user(1L, "admin@example.com", Role.ADMIN, null);
    authenticateAs(creator);
    CreateUserRequest request = request(Role.EMPLOYEE);
    when(userRepository.findByUsernameIgnoreCase(request.username())).thenReturn(Optional.empty());
    when(employeeRepository.findByEmail(request.username())).thenReturn(81L);
    when(userRepository.findByEmployeeId(81L)).thenReturn(Optional.empty());
    when(passwordEncoder.encode(request.password())).thenReturn("encoded");
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User saved = invocation.getArgument(0);
              saved.setId(2L);
              return saved;
            });

    UserResponse response = service.createUser("Bearer token", request);

    assertThat(response.employeeId()).isEqualTo(81L);
  }

  private void authenticateAs(User creator) {
    when(jwtService.authenticate("Bearer token"))
        .thenReturn(
            new JwtService.AuthenticatedUser(
                creator.getId(),
                creator.getUsername(),
                creator.getRole(),
                creator.getEmployeeId()));
    when(userRepository.findById(creator.getId())).thenReturn(Optional.of(creator));
  }

  private User user(Long id, String username, Role role, Long employeeId) {
    User user = new User(username, "hash", role, employeeId);
    user.setId(id);
    return user;
  }

  private CreateUserRequest request(Role role) {
    return new CreateUserRequest(
        "new.user@example.com",
        "password123",
        role,
        "New",
        "User",
        "Engineering",
        "Developer",
        null,
        LocalDate.of(2026, 9, 9));
  }
}
