package com.jiralite.tickets.service;

import com.jiralite.tickets.api.dto.ActivityResponse;
import com.jiralite.tickets.api.dto.ChangeStatusRequest;
import com.jiralite.tickets.api.dto.CommentResponse;
import com.jiralite.tickets.api.dto.CreateCommentRequest;
import com.jiralite.tickets.api.dto.CreateTicketRequest;
import com.jiralite.tickets.api.dto.PageResponse;
import com.jiralite.tickets.api.dto.TicketListRequest;
import com.jiralite.tickets.api.dto.TicketResponse;
import com.jiralite.tickets.api.dto.UpdateTicketRequest;
import java.util.List;
import java.util.UUID;

public interface TicketService {
    TicketResponse create(UUID actorId, CreateTicketRequest request);

    PageResponse<TicketResponse> list(UUID actorId, TicketListRequest request);

    TicketResponse get(UUID actorId, String id);

    TicketResponse patch(UUID actorId, String id, UpdateTicketRequest request);

    TicketResponse changeStatus(UUID actorId, String id, ChangeStatusRequest request);

    List<CommentResponse> comments(UUID actorId, String id);

    CommentResponse addComment(UUID actorId, String id, CreateCommentRequest request);

    List<ActivityResponse> activity(UUID actorId, String id);
}
