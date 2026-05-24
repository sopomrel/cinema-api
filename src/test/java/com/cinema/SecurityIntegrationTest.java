package com.cinema;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicCanAccessRoot() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void publicCanReadMovies() throws Exception {
        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedCannotAccessProfile() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void authenticatedUserCanAccessProfile() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk());
    }

    @Test
    void userCannotAccessAdminSummary() throws Exception {
        mockMvc.perform(get("/api/admin/summary").with(httpBasic("user", "user123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessAdminSummary() throws Exception {
        mockMvc.perform(get("/api/admin/summary").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk());
    }

    @Test
    void userCannotDeleteMovie() throws Exception {
        mockMvc.perform(delete("/api/movies/1").with(httpBasic("user", "user123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedCannotCreateMovie() throws Exception {
        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Test",
                                  "releaseYear": 2020,
                                  "genre": "Drama",
                                  "rating": 8.0,
                                  "directorId": 1
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }
}
