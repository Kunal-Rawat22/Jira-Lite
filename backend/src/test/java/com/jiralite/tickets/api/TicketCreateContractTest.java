package com.jiralite.tickets.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jiralite.tickets.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class TicketCreateContractTest extends IntegrationTest {
    @Test
    void createEnvelopeRejectsReporterAndStatus() throws Exception {
        String token = login("bob");
        mockMvc.perform(
                        post("/api/tickets")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\":\"Need help\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andExpect(jsonPath("$.data.version").value(1));
        mockMvc.perform(
                        post("/api/tickets")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\":\"x\",\"reporterId\":\"00000000-0000-0000-0000-0000000000a1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}

class TicketCreateValidationTest extends IntegrationTest {
    @Test
    void blankAndOverlong() throws Exception {
        String token = login("bob");
        mockMvc.perform(
                        post("/api/tickets")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\":\"   \"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(
                        post("/api/tickets")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\":\"" + "a".repeat(41) + "\"}"))
                .andExpect(status().isBadRequest());
    }
}

class TicketCreateProductTest extends IntegrationTest {
    @Test
    void aliceNeedsProductBobDoesNot() throws Exception {
        String alice = login("alice");
        mockMvc.perform(
                        post("/api/tickets")
                                .header("Authorization", "Bearer " + alice)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\":\"Need product\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(
                        post("/api/tickets")
                                .header("Authorization", "Bearer " + alice)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"title\":\"With product\",\"productId\":\"00000000-0000-0000-0000-000000000001\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"x\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));
    }
}
