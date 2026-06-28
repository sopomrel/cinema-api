package com.cinema.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler unit tests")
class GlobalExceptionHandlerTest {

    @Mock
    private org.springframework.context.MessageSource messageSource;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler(messageSource);
    }

    @Test
    @DisplayName("ResourceNotFoundException returns 404 with localized message")
    void handleResourceNotFound_returnsNotFound() {
        when(messageSource.getMessage(eq("error.actor.notFound"), any(), eq("error.actor.notFound"), any(Locale.class)))
                .thenReturn("Actor not found with ID: 1");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(
                new ResourceNotFoundException("error.actor.notFound", 1L),
                Locale.ENGLISH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Actor not found");
    }

    @Test
    @DisplayName("DuplicateResourceException returns 409 with localized message")
    void handleDuplicateResource_returnsConflict() {
        when(messageSource.getMessage(eq("error.actor.duplicateEmail"), any(), eq("error.actor.duplicateEmail"), any(Locale.class)))
                .thenReturn("Actor with email leo@example.com already exists");

        ResponseEntity<ErrorResponse> response = handler.handleDuplicateResourceException(
                new DuplicateResourceException("error.actor.duplicateEmail", "leo@example.com"),
                Locale.ENGLISH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
    }

    @Test
    @DisplayName("AccessDeniedException returns 403 with localized message")
    void handleAccessDenied_returnsForbidden() {
        when(messageSource.getMessage(eq("error.access.denied"), any(), eq("error.access.denied"), any(Locale.class)))
                .thenReturn("Access denied: insufficient permissions");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(
                new AccessDeniedException("denied"),
                Locale.ENGLISH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Access denied");
    }
}
