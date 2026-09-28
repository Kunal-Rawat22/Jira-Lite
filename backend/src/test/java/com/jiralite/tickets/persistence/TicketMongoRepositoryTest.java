package com.jiralite.tickets.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.jiralite.tickets.IntegrationTest;
import com.jiralite.tickets.domain.Ticket;
import com.jiralite.tickets.domain.TicketPriority;
import com.jiralite.tickets.domain.TicketStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TicketMongoRepositoryTest extends IntegrationTest {
    @Autowired
    TicketRepository tickets;

    @Test
    void persistTicket() {
        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID().toString());
        ticket.setProductId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        ticket.setTitle("mongo");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(TicketPriority.MEDIUM);
        ticket.setReporterId(UUID.fromString("00000000-0000-0000-0000-0000000000b2"));
        ticket.setAssigneeId(UUID.fromString("00000000-0000-0000-0000-0000000000b2"));
        ticket.setVersion(1);
        ticket.setCreatedAt(Instant.now());
        ticket.setUpdatedAt(Instant.now());
        tickets.save(ticket);
        assertThat(tickets.findById(ticket.getId())).isPresent();
    }
}
