package com.jiralite.tickets.api.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(String id, String ticketId, UUID authorId, String body, Instant createdAt) {}
