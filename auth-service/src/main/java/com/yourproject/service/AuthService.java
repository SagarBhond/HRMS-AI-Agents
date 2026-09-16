package com.yourproject.service;

import com.yourproject.config.JwtService;
import com.yourproject.config.JwtService.AuthenticatedUser;
import com.yourproject.dto.CreateUserRequest;
import com.yourproject.dto.LoginRequest;
import com.yourproject.dto.LoginResponse;
import com.yourproject.dto.UserResponse;
import com.yourproject.entity.Role;
import com.yourproject.entity.User;
import com.yourproject.repository.EmployeeAccountRepository;
import com.yourproject.repository.UserRepository;
import java.util.Map;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private static final Map<Role, Set<Role>> CREATABLE_ROLES =
      Map.of(
          Role.ADMIN, Set.of(Role.MANAGER, Role.HR, Role.EMPLOYEE),
          Role.MANAGER, Set.of(Role.HR, Role.EMPLOYEE),
          Role.HR, Set.of(Role.EMPLOYEE),
          Role.EMPLOYEE, Set.of());

  private final UserRepository userRepository;
  private final EmployeeAccountRepository employeeRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(
      UserRepository userRepository,
      EmployeeAccountRepository employeeRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService) {
    this.userRepository = userRepository;
    this.employeeRepository = employeeRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  @Transactional(readOnly = true)
  public LoginResponse login(LoginRequest request) {
    String username = normalizeUsername(request.getUsername());
    User user =
        userRepository
            .findByUsernameIgnoreCase(username)
            .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new InvalidCredentialsException("Invalid username or password");
    }

    String token = jwtService.generateToken(user);
    return new LoginResponse(
        user.getId(),
        user.getUsername(),
        user.getRole().name(),
        user.getEmployeeId(),
        "Login successful",
        token);
  }

  @Transactional
  public UserResponse createUser(String authorizationHeader, CreateUserRequest request) {
    User creator = currentPersistedUser(authorizationHeader);
    requireCanCreate(creator.getRole(), request.role());

    String username = normalizeUsername(request.username());
    if (userRepository.findByUsernameIgnoreCase(username).isPresent()) {
      throw new DuplicateUserException("A login already exists for " + username);
    }

    validateManager(request.managerEmployeeId());
    Long employeeId = employeeRepository.findByEmail(username);
    if (employeeId == null) {
      employeeId = employeeRepository.create(request);
    } else if (userRepository.findByEmployeeId(employeeId).isPresent()) {
      throw new DuplicateUserException("Employee " + employeeId + " already has a login");
    }

    User user =
        new User(username, passwordEncoder.encode(request.password()), request.role(), employeeId);
    User saved = userRepository.save(user);
    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public UserResponse currentUser(String authorizationHeader) {
    return toResponse(currentPersistedUser(authorizationHeader));
  }

  @Transactional
  public void linkEmployee(String authorizationHeader, Long userId, Long employeeId) {
    User creator = currentPersistedUser(authorizationHeader);
    if (creator.getRole() != Role.ADMIN) {
      throw new ForbiddenOperationException("Only ADMIN can repair an employee-account link");
    }
    if (!employeeRepository.existsById(employeeId)) {
      throw new AccountProvisioningException("No employee found with id " + employeeId, null);
    }
    userRepository
        .findByEmployeeId(employeeId)
        .ifPresent(
            existing -> {
              if (!existing.getId().equals(userId)) {
                throw new DuplicateUserException("Employee " + employeeId + " already has a login");
              }
            });

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new InvalidCredentialsException("No user with id " + userId));
    user.setEmployeeId(employeeId);
    userRepository.save(user);
  }

  private User currentPersistedUser(String authorizationHeader) {
    AuthenticatedUser tokenUser = jwtService.authenticate(authorizationHeader);
    User user =
        userRepository
            .findById(tokenUser.userId())
            .orElseThrow(
                () -> new InvalidCredentialsException("Authenticated user no longer exists"));
    if (!user.getUsername().equalsIgnoreCase(tokenUser.username())) {
      throw new InvalidCredentialsException("Token identity does not match the current account");
    }
    return user;
  }

  private void requireCanCreate(Role creatorRole, Role requestedRole) {
    if (!CREATABLE_ROLES.getOrDefault(creatorRole, Set.of()).contains(requestedRole)) {
      throw new ForbiddenOperationException(
          creatorRole + " is not allowed to create a " + requestedRole + " account");
    }
  }

  private void validateManager(Long managerEmployeeId) {
    if (managerEmployeeId == null) {
      return;
    }
    User manager =
        userRepository
            .findByEmployeeId(managerEmployeeId)
            .orElseThrow(
                () ->
                    new AccountProvisioningException(
                        "Manager employee id " + managerEmployeeId + " is not linked to a login",
                        null));
    if (manager.getRole() != Role.MANAGER && manager.getRole() != Role.ADMIN) {
      throw new AccountProvisioningException(
          "Employee " + managerEmployeeId + " does not have the MANAGER role", null);
    }
  }

  private String normalizeUsername(String username) {
    return username.trim().toLowerCase();
  }

  private UserResponse toResponse(User user) {
    return new UserResponse(user.getId(), user.getUsername(), user.getRole(), user.getEmployeeId());
  }
}
