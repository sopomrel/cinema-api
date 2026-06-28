package com.cinema.controller;

import com.cinema.config.ValidationConfig;
import com.cinema.dto.response.ActorResponse;
import com.cinema.exception.GlobalExceptionHandler;
import com.cinema.service.ActorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ActorController.class)
@Import({GlobalExceptionHandler.class, ValidationConfig.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ActorController web layer tests")
class ActorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActorService actorService;

    @Test
    @DisplayName("GET /api/actors returns list of actors")
    void getAllActors_returnsOk() throws Exception {
        when(actorService.getAllActors()).thenReturn(List.of(
                ActorResponse.builder().id(1L).firstName("Leo").lastName("DiCaprio").email("leo@example.com").build()
        ));

        mockMvc.perform(get("/api/actors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Leo"));
    }

    @Test
    @DisplayName("GET /api/actors/{id} returns actor when found")
    void getActorById_returnsOk() throws Exception {
        when(actorService.getActorById(1L)).thenReturn(
                ActorResponse.builder().id(1L).firstName("Leo").lastName("DiCaprio").email("leo@example.com").build()
        );

        mockMvc.perform(get("/api/actors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("leo@example.com"));
    }

    @Test
    @DisplayName("POST /api/actors with invalid body returns 400")
    void createActor_invalidRequest_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/actors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "A",
                                  "lastName": "B",
                                  "email": "not-an-email"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @DisplayName("POST /api/actors with valid body returns 201")
    void createActor_validRequest_returnsCreated() throws Exception {
        when(actorService.createActor(any())).thenReturn(
                ActorResponse.builder().id(1L).firstName("Leo").lastName("DiCaprio").email("leo@example.com").build()
        );

        mockMvc.perform(post("/api/actors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Leonardo",
                                  "lastName": "DiCaprio",
                                  "email": "leo@example.com",
                                  "nationality": "American"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /api/actors/{id} delegates to service")
    void updateActor_returnsOk() throws Exception {
        when(actorService.updateActor(eq(1L), any())).thenReturn(
                ActorResponse.builder().id(1L).firstName("Leonardo").lastName("DiCaprio").email("leo@example.com").build()
        );

        mockMvc.perform(put("/api/actors/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Leonardo",
                                  "lastName": "DiCaprio",
                                  "email": "leo@example.com"
                                }
                                """))
                .andExpect(status().isOk());

        verify(actorService).updateActor(eq(1L), any());
    }
}
