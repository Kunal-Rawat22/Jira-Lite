package com.jiralite.tickets.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jiralite.tickets.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class TicketPatchPartialUpdateTest extends IntegrationTest {
    @Test
    void titleOnlyPreservesOtherFieldsAndBumpsVersionOnce() throws Exception {
        String token = login("bob");
        String id = createTicket(token, "Old title");
        mockMvc.perform(
                        patch("/api/tickets/" + id)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"version\":1,\"title\":\"New title\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("New title"))
                .andExpect(jsonPath("$.data.version").value(2))
                .andExpect(jsonPath("$.data.priority").value("MEDIUM"));
    }
}

class TicketPatchRejectsStatusTest extends IntegrationTest {
    @Test
    void statusInPatch() throws Exception {
        String token = login("bob");
        String id = createTicket(token, "Keep status");
        mockMvc.perform(
                        patch("/api/tickets/" + id)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"version\":1,\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}

class TicketPatchVersionTest extends IntegrationTest {
    @Test
    void staleDoesNotChange() throws Exception {
        String token = login("bob");
        String id = createTicket(token, "Versioned");
        mockMvc.perform(
                        patch("/api/tickets/" + id)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"version\":99,\"title\":\"Nope\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("STALE_VERSION"));
        mockMvc.perform(
                        patch("/api/tickets/" + id)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"version\":1,\"title\":\"Ok\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.version").value(2));
    }
}

class TicketStatusContractTest extends IntegrationTest {
    @Test
    void statusRequiresVersion() throws Exception {
        String token = login("bob");
        String id = createTicket(token, "Move me");
        mockMvc.perform(
                        post("/api/tickets/" + id + "/status")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"version\":1,\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.version").value(2));
    }
}
