package com.jiralite.tickets.error;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jiralite.tickets.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ApiResponseEnvelopeTest extends IntegrationTest {
    @Test
    void failedLoginEnvelope() throws Exception {
        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"nope\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"))
                .andExpect(jsonPath("$.status").value("failed"))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
