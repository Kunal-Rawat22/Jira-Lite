package com.jiralite.tickets.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jiralite.tickets.error.ApiException;
import com.jiralite.tickets.error.ErrorCodes;
import org.junit.jupiter.api.Test;

class TicketValidationTest {
    private final TicketValidation validation = new TicketValidation();

    @Test
    void unknownPriorityIsValidationError() {
        assertThatThrownBy(() -> validation.parsePriority("URGENT", null))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo(ErrorCodes.VALIDATION_ERROR);
    }

    @Test
    void unknownStatusIsValidationError() {
        assertThatThrownBy(() -> validation.parseStatus("DONE"))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo(ErrorCodes.VALIDATION_ERROR);
    }
}
