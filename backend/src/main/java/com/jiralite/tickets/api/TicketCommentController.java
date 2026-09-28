package com.jiralite.tickets.api;

import com.jiralite.tickets.api.dto.ApiResponse;
import com.jiralite.tickets.api.dto.CommentResponse;
import com.jiralite.tickets.api.dto.CreateCommentRequest;
import com.jiralite.tickets.error.MessageResolver;
import com.jiralite.tickets.service.TicketService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets/{id}/comments")
public class TicketCommentController {
    private final TicketService tickets;
    private final MessageResolver messages;

    public TicketCommentController(TicketService tickets, MessageResolver messages) {
        this.tickets = tickets;
        this.messages = messages;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponse>>> list(@PathVariable String id) {
        return ResponseEntity.ok(
                ApiResponse.success(messages.message("SUCCESS"), "SUCCESS", tickets.comments(CurrentUser.id(), id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> create(
            @PathVariable String id, @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                messages.message("SUCCESS"), "SUCCESS", tickets.addComment(CurrentUser.id(), id, request)));
    }
}
