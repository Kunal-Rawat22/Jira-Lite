package com.jiralite.tickets.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.jiralite.tickets.domain.TicketStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TicketStateMachineAllowedTest {
    private final TicketStateMachine machine = new TicketStateMachine();

    @ParameterizedTest
    @CsvSource({
        "OPEN,IN_PROGRESS",
        "OPEN,CANCELLED",
        "IN_PROGRESS,RESOLVED",
        "IN_PROGRESS,CANCELLED",
        "RESOLVED,CLOSED",
        "RESOLVED,REOPEN",
        "CLOSED,REOPEN",
        "CANCELLED,REOPEN",
        "REOPEN,IN_PROGRESS",
        "REOPEN,CANCELLED"
    })
    void allowedTransitions(TicketStatus from, TicketStatus to) {
        assertThat(machine.allowed(from, to)).isTrue();
    }
}
