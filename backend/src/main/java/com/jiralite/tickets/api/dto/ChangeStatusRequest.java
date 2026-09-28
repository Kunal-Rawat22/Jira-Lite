package com.jiralite.tickets.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChangeStatusRequest(@NotNull Integer version, @NotBlank String status) {}
