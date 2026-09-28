package com.jiralite.tickets.domain;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "activities")
public class Activity {

    @Id
    private String id;

    private String ticketId;
    private UUID actorId;
    private Instant at;
    private String field;
    private Map<String, Object> from;
    private Map<String, Object> to;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public Instant getAt() {
        return at;
    }

    public void setAt(Instant at) {
        this.at = at;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Map<String, Object> getFrom() {
        return from;
    }

    public void setFrom(Map<String, Object> from) {
        this.from = from;
    }

    public Map<String, Object> getTo() {
        return to;
    }

    public void setTo(Map<String, Object> to) {
        this.to = to;
    }
}
