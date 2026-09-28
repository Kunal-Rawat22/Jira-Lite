package com.jiralite.tickets.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jiralite.tickets.domain.TicketStatus;
import com.jiralite.tickets.error.ApiException;
import com.jiralite.tickets.error.ErrorCodes;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TicketStateMachineRejectedTest {
    private final TicketStateMachine machine = new TicketStateMachine();

    @ParameterizedTest
    @CsvSource({
        "OPEN,OPEN",
        "CLOSED,OPEN",
        "RESOLVED,OPEN",
        "CANCELLED,OPEN",
        "IN_PROGRESS,CLOSED",
        "IN_PROGRESS,IN_PROGRESS",
        "REOPEN,OPEN"
    })
    void rejectedPairs(TicketStatus from, TicketStatus to) {
        assertThat(machine.allowed(from, to)).isFalse();
        assertThatThrownBy(() -> machine.requireAllowed(from, to))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo(ErrorCodes.INVALID_STATE_TRANSITION);
    }
}
