package com.jiralite.tickets.persistence;

import com.jiralite.tickets.domain.Activity;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ActivityRepository extends MongoRepository<Activity, String> {
    List<Activity> findByTicketIdOrderByAtAsc(String ticketId);
}
