package com.yogendra.reservation_system.integration;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorrelationIdIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(
            username = "john",
            roles = "USER"
    )
    void request_ShouldReturnGeneratedCorrelationId()
            throws Exception {

        mockMvc.perform(
                        get("/rooms")
                )
                .andExpect(status().isOk())
                .andExpect(
                        header().string(
                                "X-Correlation-Id",
                                not(blankOrNullString())
                        )
                );
    }
    @Test
    @WithMockUser(
            username = "john",
            roles = "USER"
    )
    void request_ShouldPreserveExistingCorrelationId()
            throws Exception {

        String correlationId = "client-request-123";

        mockMvc.perform(
                        get("/rooms")
                                .header(
                                        "X-Correlation-Id",
                                        correlationId
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        header().string(
                                "X-Correlation-Id",
                                correlationId
                        )
                );
    }
}