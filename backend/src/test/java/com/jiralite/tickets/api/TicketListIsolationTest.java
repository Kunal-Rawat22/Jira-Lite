package com.jiralite.tickets.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jiralite.tickets.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class TicketListIsolationTest extends IntegrationTest {
    @Test
    void danaDoesNotSeeSupportTickets() throws Exception {
        String bob = login("bob");
        createTicket(bob, "Support only ticket");
        String dana = login("dana");
        mockMvc.perform(
                        post("/api/tickets/list")
                                .header("Authorization", "Bearer " + dana)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.title=='Support only ticket')]").doesNotExist());
    }
}

class TicketListContractTest extends IntegrationTest {
    @Test
    void listIsPostJsonZeroBasedPage() throws Exception {
        String bob = login("bob");
        mockMvc.perform(
                        post("/api/tickets/list")
                                .header("Authorization", "Bearer " + bob)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"page\":0,\"size\":20,\"reporter\":[],\"searchKey\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.content").isArray());
        mockMvc.perform(get("/api/tickets/list").header("Authorization", "Bearer " + bob))
                .andExpect(status().is4xxClientError());
    }
}

class CommentDoesNotCreateActivityTest extends IntegrationTest {
    @Test
    void commentSkipsActivity() throws Exception {
        String bob = login("bob");
        String id = createTicket(bob, "Commented");
        mockMvc.perform(
                        post("/api/tickets/" + id + "/comments")
                                .header("Authorization", "Bearer " + bob)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"body\":\"hello there\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/tickets/" + id + "/activity").header("Authorization", "Bearer " + bob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}

class ActivityFieldUpdateTest extends IntegrationTest {
    @Test
    void oneActivityWithChangedTitleOnly() throws Exception {
        String bob = login("bob");
        String id = createTicket(bob, "Old title");
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/tickets/" + id)
                                .header("Authorization", "Bearer " + bob)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"version\":1,\"title\":\"New title\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/tickets/" + id + "/activity").header("Authorization", "Bearer " + bob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].from.title").value("Old title"))
                .andExpect(jsonPath("$.data[0].to.title").value("New title"))
                .andExpect(jsonPath("$.data[0].from.priority").doesNotExist());
    }
}

class TicketFrozenUpdateTest extends IntegrationTest {
    @Test
    void cancelledRejectsPatch() throws Exception {
        String bob = login("bob");
        String id = createTicket(bob, "Freeze me");
        mockMvc.perform(
                        post("/api/tickets/" + id + "/status")
                                .header("Authorization", "Bearer " + bob)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"version\":1,\"status\":\"CANCELLED\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/tickets/" + id)
                                .header("Authorization", "Bearer " + bob)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"version\":2,\"title\":\"nope\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TICKET_FROZEN"));
    }
}
