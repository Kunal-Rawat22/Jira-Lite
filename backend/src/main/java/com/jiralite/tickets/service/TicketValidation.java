package com.jiralite.tickets.service;

import com.jiralite.tickets.domain.TicketPriority;
import com.jiralite.tickets.domain.TicketStatus;
import com.jiralite.tickets.error.ApiException;
import com.jiralite.tickets.error.ErrorCodes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class TicketValidation {
    public void requireTitle(String title) {
        if (title == null || title.isBlank() || title.length() > 40) {
            throw new ApiException(ErrorCodes.VALIDATION_ERROR, HttpStatus.BAD_REQUEST);
        }
    }

    public void requireDescription(String description) {
        if (description != null && description.length() > 1000) {
            throw new ApiException(ErrorCodes.VALIDATION_ERROR, HttpStatus.BAD_REQUEST);
        }
    }

    public void requireComment(String body) {
        if (body == null || body.isBlank() || body.length() > 200) {
            throw new ApiException(ErrorCodes.VALIDATION_ERROR, HttpStatus.BAD_REQUEST);
        }
    }

    public TicketPriority parsePriority(String raw, TicketPriority defaultValue) {
        if (raw == null || raw.isEmpty()) {
            return defaultValue;
        }
        try {
            return TicketPriority.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(ErrorCodes.VALIDATION_ERROR, HttpStatus.BAD_REQUEST);
        }
    }

    public TicketStatus parseStatus(String raw) {
        if (raw == null) {
            throw new ApiException(ErrorCodes.VALIDATION_ERROR, HttpStatus.BAD_REQUEST);
        }
        try {
            return TicketStatus.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(ErrorCodes.VALIDATION_ERROR, HttpStatus.BAD_REQUEST);
        }
    }
}
