package com.jiralite.tickets.api.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ActivityResponse(
        String id,
        String ticketId,
        UUID actorId,
        Instant at,
        String field,
        Map<String, Object> from,
        Map<String, Object> to) {}
