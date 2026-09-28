package com.jiralite.tickets.api.dto;

import jakarta.validation.constraints.NotNull;

public record CreateCommentRequest(@NotNull String body) {}
