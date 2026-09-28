package com.jiralite.tickets.service;

import com.jiralite.tickets.domain.Activity;
import com.jiralite.tickets.domain.Ticket;
import com.jiralite.tickets.domain.TicketStatus;
import com.jiralite.tickets.persistence.ActivityRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ActivityRecorder {
    private final ActivityRepository activities;

    public ActivityRecorder(ActivityRepository activities) {
        this.activities = activities;
    }

    public void recordFields(Ticket ticket, UUID actorId, Map<String, Object> from, Map<String, Object> to) {
        if (from.isEmpty()) {
            return;
        }
        Activity activity = new Activity();
        activity.setId(UUID.randomUUID().toString());
        activity.setTicketId(ticket.getId());
        activity.setActorId(actorId);
        activity.setAt(Instant.now());
        activity.setField("fields");
        activity.setFrom(from);
        activity.setTo(to);
        activities.save(activity);
    }

    public void recordStatus(Ticket ticket, UUID actorId, TicketStatus from, TicketStatus to) {
        Map<String, Object> fromMap = new LinkedHashMap<>();
        Map<String, Object> toMap = new LinkedHashMap<>();
        fromMap.put("status", from.name());
        toMap.put("status", to.name());
        Activity activity = new Activity();
        activity.setId(UUID.randomUUID().toString());
        activity.setTicketId(ticket.getId());
        activity.setActorId(actorId);
        activity.setAt(Instant.now());
        activity.setField("status");
        activity.setFrom(fromMap);
        activity.setTo(toMap);
        activities.save(activity);
    }
}
