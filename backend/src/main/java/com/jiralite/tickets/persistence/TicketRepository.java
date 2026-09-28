package com.jiralite.tickets.persistence;

import com.jiralite.tickets.domain.Ticket;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TicketRepository extends MongoRepository<Ticket, String> {}
