package com.jiralite.tickets.service;

import com.jiralite.tickets.domain.TicketStatus;
import com.jiralite.tickets.error.ApiException;
import com.jiralite.tickets.error.ErrorCodes;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class TicketStateMachine {
    private record Edge(TicketStatus from, TicketStatus to) {}

    private static final Set<Edge> ALLOWED =
            Set.of(
                    new Edge(TicketStatus.OPEN, TicketStatus.IN_PROGRESS),
                    new Edge(TicketStatus.OPEN, TicketStatus.CANCELLED),
                    new Edge(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED),
                    new Edge(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED),
                    new Edge(TicketStatus.RESOLVED, TicketStatus.CLOSED),
                    new Edge(TicketStatus.RESOLVED, TicketStatus.REOPEN),
                    new Edge(TicketStatus.CLOSED, TicketStatus.REOPEN),
                    new Edge(TicketStatus.CANCELLED, TicketStatus.REOPEN),
                    new Edge(TicketStatus.REOPEN, TicketStatus.IN_PROGRESS),
                    new Edge(TicketStatus.REOPEN, TicketStatus.CANCELLED));

    public boolean allowed(TicketStatus from, TicketStatus to) {
        return ALLOWED.contains(new Edge(from, to));
    }

    public void requireAllowed(TicketStatus from, TicketStatus to) {
        if (!allowed(from, to)) {
            throw new ApiException(ErrorCodes.INVALID_STATE_TRANSITION, HttpStatus.CONFLICT);
        }
    }
}
