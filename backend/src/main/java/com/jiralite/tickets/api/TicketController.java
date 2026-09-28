package com.jiralite.tickets.api;

import com.jiralite.tickets.api.dto.ApiResponse;
import com.jiralite.tickets.api.dto.ChangeStatusRequest;
import com.jiralite.tickets.api.dto.CreateTicketRequest;
import com.jiralite.tickets.api.dto.PageResponse;
import com.jiralite.tickets.api.dto.TicketListRequest;
import com.jiralite.tickets.api.dto.TicketResponse;
import com.jiralite.tickets.api.dto.UpdateTicketRequest;
import com.jiralite.tickets.error.MessageResolver;
import com.jiralite.tickets.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService tickets;
    private final MessageResolver messages;

    public TicketController(TicketService tickets, MessageResolver messages) {
        this.tickets = tickets;
        this.messages = messages;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponse>> create(@RequestBody CreateTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ok(tickets.create(CurrentUser.id(), request)));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageResponse<TicketResponse>>> list(
            @RequestBody(required = false) TicketListRequest request) {
        TicketListRequest body = request == null ? new TicketListRequest() : request;
        return ResponseEntity.ok(ok(tickets.list(CurrentUser.id(), body)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> get(@PathVariable String id) {
        return ResponseEntity.ok(ok(tickets.get(CurrentUser.id(), id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> patch(
            @PathVariable String id, @Valid @RequestBody UpdateTicketRequest request) {
        return ResponseEntity.ok(ok(tickets.patch(CurrentUser.id(), id, request)));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TicketResponse>> status(
            @PathVariable String id, @Valid @RequestBody ChangeStatusRequest request) {
        return ResponseEntity.ok(ok(tickets.changeStatus(CurrentUser.id(), id, request)));
    }

    private <T> ApiResponse<T> ok(T data) {
        return ApiResponse.success(messages.message("SUCCESS"), "SUCCESS", data);
    }
}
