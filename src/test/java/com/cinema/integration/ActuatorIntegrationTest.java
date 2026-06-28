package com.cinema.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@DisplayName("Actuator integration tests")
class ActuatorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /actuator/health is public and returns UP")
    void healthEndpoint_isPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("GET /actuator/health includes custom cinema health indicator")
    void healthEndpoint_includesCustomIndicator() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.cinema.status").value("UP"));
    }

    @Test
    @DisplayName("GET /actuator/health details available to ADMIN")
    void healthEndpoint_detailsForAdmin() throws Exception {
        mockMvc.perform(get("/actuator/health").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components").exists());
    }

    @Test
    @DisplayName("GET /actuator/info requires ADMIN role")
    void infoEndpoint_requiresAdmin() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/actuator/info").with(httpBasic("user", "user123")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/actuator/info").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name").value("Cinema API (Development)"));
    }

    @Test
    @DisplayName("GET /actuator/metrics requires ADMIN role")
    void metricsEndpoint_requiresAdmin() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/actuator/metrics").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names").isArray());
    }

    @Test
    @DisplayName("GET /actuator/metrics/cinema.movies.created tracks movie creation")
    void customMetric_isAvailableAfterMovieCreation() throws Exception {
        mockMvc.perform(post("/api/movies")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Test Movie",
                                  "releaseYear": 2021,
                                  "genre": "Drama",
                                  "rating": 7.5,
                                  "directorId": 1
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/actuator/metrics/cinema.movies.created")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("cinema.movies.created"))
                .andExpect(jsonPath("$.measurements[0].value").value(1.0));
    }

    @Test
    @DisplayName("GET / with Accept-Language returns localized welcome")
    void homeEndpoint_supportsI18n() throws Exception {
        mockMvc.perform(get("/").header("Accept-Language", "en"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Use the links below")));
    }
}
