package com.jiralite.tickets.api;

import com.jiralite.tickets.api.dto.ActivityResponse;
import com.jiralite.tickets.api.dto.ApiResponse;
import com.jiralite.tickets.error.MessageResolver;
import com.jiralite.tickets.service.TicketService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets/{id}/activity")
public class TicketActivityController {
    private final TicketService tickets;
    private final MessageResolver messages;

    public TicketActivityController(TicketService tickets, MessageResolver messages) {
        this.tickets = tickets;
        this.messages = messages;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> list(@PathVariable String id) {
        return ResponseEntity.ok(
                ApiResponse.success(messages.message("SUCCESS"), "SUCCESS", tickets.activity(CurrentUser.id(), id)));
    }
}
