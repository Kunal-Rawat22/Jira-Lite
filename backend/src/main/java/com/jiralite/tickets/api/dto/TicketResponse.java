package com.jiralite.tickets.api.dto;

import java.time.Instant;
import java.util.UUID;

public record TicketResponse(
        String id,
        UUID productId,
        String title,
        String description,
        String status,
        String priority,
        UUID reporterId,
        UUID assigneeId,
        int version,
        Instant createdAt,
        Instant updatedAt) {}
