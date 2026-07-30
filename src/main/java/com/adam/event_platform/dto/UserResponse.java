package com.adam.event_platform.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(
    Long id,
    String username,
    String email,
    Set<String> roles,
    LocalDateTime createdAt
) {}
