package com.jiralite.tickets.api.dto;

import java.util.UUID;

public record LoginResponse(String token, UserResponse user) {}
