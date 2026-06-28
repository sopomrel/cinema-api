package com.cinema.integration;

import com.cinema.dto.request.DirectorRequest;
import com.cinema.dto.response.DirectorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Black-box integration tests that exercise the full HTTP stack (embedded server,
 * filters, security, JSON serialization) using {@link TestRestTemplate}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@DisplayName("End-to-end API tests (TestRestTemplate)")
class ApiEndToEndTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("GET /actuator/health is publicly reachable and reports UP")
    void healthEndpoint_isPublic() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    @DisplayName("GET /api/movies is public in dev and returns seeded data")
    void getMovies_isPublic() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/movies", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Inception");
    }

    @Test
    @DisplayName("POST /api/movies without authentication is rejected with 401")
    void createMovie_unauthenticated_isUnauthorized() {
        String payload = """
                {"title":"Test","releaseYear":2020,"genre":"Drama","rating":8.0,"directorId":1}
                """;

        ResponseEntity<String> response = restTemplate.postForEntity("/api/movies", jsonEntity(payload), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("GET /actuator/info is forbidden for USER but allowed for ADMIN")
    void infoEndpoint_isAdminOnly() {
        ResponseEntity<String> userResponse = restTemplate
                .withBasicAuth("user", "user123")
                .getForEntity("/actuator/info", String.class);
        assertThat(userResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<String> adminResponse = restTemplate
                .withBasicAuth("admin", "admin123")
                .getForEntity("/actuator/info", String.class);
        assertThat(adminResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(adminResponse.getBody()).contains("Cinema API");
    }

    @Test
    @DisplayName("ADMIN can create a director and read it back")
    void createDirector_asAdmin_succeeds() {
        DirectorRequest request = DirectorRequest.builder()
                .firstName("Greta")
                .lastName("Gerwig")
                .email("greta-" + UUID.randomUUID() + "@example.com")
                .nationality("American")
                .build();

        ResponseEntity<DirectorResponse> created = restTemplate
                .withBasicAuth("admin", "admin123")
                .postForEntity("/api/directors", request, DirectorResponse.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().getId()).isNotNull();

        Long id = created.getBody().getId();
        ResponseEntity<DirectorResponse> fetched = restTemplate
                .getForEntity("/api/directors/" + id, DirectorResponse.class);

        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody()).isNotNull();
        assertThat(fetched.getBody().getLastName()).isEqualTo("Gerwig");
    }

    @Test
    @DisplayName("USER cannot create a director (ADMIN-only) and receives 403")
    void createDirector_asUser_isForbidden() {
        DirectorRequest request = DirectorRequest.builder()
                .firstName("Denis")
                .lastName("Villeneuve")
                .email("denis-" + UUID.randomUUID() + "@example.com")
                .nationality("Canadian")
                .build();

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("user", "user123")
                .postForEntity("/api/directors", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private static org.springframework.http.HttpEntity<String> jsonEntity(String body) {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return new org.springframework.http.HttpEntity<>(body, headers);
    }
}
