package com.jiralite.tickets.api.dto;

import java.util.UUID;

public record UserResponse(UUID id, String username, String email, String displayName) {}
