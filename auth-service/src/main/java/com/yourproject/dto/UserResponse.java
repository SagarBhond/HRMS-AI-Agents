package com.yourproject.dto;

import com.yourproject.entity.Role;

public record UserResponse(Long userId, String username, Role role, Long employeeId) {}
