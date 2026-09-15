package com.adam.event_platform.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateEventRequest(
    @NotBlank String title,
    @NotBlank String description,
    @NotNull @Positive Integer capacity,
    LocalDateTime startTime
) {}