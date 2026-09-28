package com.jiralite.tickets.api.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class UpdateTicketRequest {
    @NotNull
    private Integer version;

    private String title;
    private boolean titlePresent;
    private String description;
    private boolean descriptionPresent;
    private String priority;
    private boolean priorityPresent;
    private UUID assigneeId;
    private boolean assigneeIdPresent;
    private boolean statusPresent;

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.titlePresent = true;
    }

    public boolean isTitlePresent() {
        return titlePresent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.descriptionPresent = true;
    }

    public boolean isDescriptionPresent() {
        return descriptionPresent;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
        this.priorityPresent = true;
    }

    public boolean isPriorityPresent() {
        return priorityPresent;
    }

    public UUID getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(UUID assigneeId) {
        this.assigneeId = assigneeId;
        this.assigneeIdPresent = true;
    }

    public boolean isAssigneeIdPresent() {
        return assigneeIdPresent;
    }

    @JsonIgnore
    public boolean isStatusPresent() {
        return statusPresent;
    }

    @JsonAnySetter
    public void any(String key, Object value) {
        if ("status".equals(key)) {
            this.statusPresent = true;
        }
    }
}
