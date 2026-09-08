package com.adam.event_platform.dto;

import com.adam.event_platform.security.Role;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(
    Long id,
    String username,
    String email,
    Set<Role> roles,
    LocalDateTime createdAt
) {}
