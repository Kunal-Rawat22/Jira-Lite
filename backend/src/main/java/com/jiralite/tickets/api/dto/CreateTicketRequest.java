package com.jiralite.tickets.api.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.jiralite.tickets.error.ApiException;
import com.jiralite.tickets.error.ErrorCodes;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class CreateTicketRequest {
    private String title;
    private String description;
    private String priority;
    private UUID assigneeId;
    private UUID productId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public UUID getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(UUID assigneeId) {
        this.assigneeId = assigneeId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    @JsonAnySetter
    public void rejectUnknown(String key, Object value) {
        if ("reporterId".equals(key) || "status".equals(key)) {
            throw new ApiException(ErrorCodes.VALIDATION_ERROR, HttpStatus.BAD_REQUEST);
        }
    }
}
