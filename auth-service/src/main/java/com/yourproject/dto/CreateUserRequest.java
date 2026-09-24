package com.yourproject.dto;

import com.yourproject.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateUserRequest(
    @NotBlank @Email String username,
    @NotBlank @Size(min = 8, max = 100) String password,
    @NotNull Role role,
    @NotBlank @Size(max = 100) String firstName,
    @Size(max = 100) String lastName,
    @Size(max = 100) String department,
    @Size(max = 100) String designation,
    Long managerEmployeeId,
    LocalDate dateOfJoining) {}
