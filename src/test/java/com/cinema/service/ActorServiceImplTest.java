package com.cinema.service;

import com.cinema.dto.request.ActorRequest;
import com.cinema.dto.response.ActorResponse;
import com.cinema.entity.Actor;
import com.cinema.exception.DuplicateResourceException;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.repository.ActorRepository;
import com.cinema.service.impl.ActorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActorService unit tests")
class ActorServiceImplTest {

    @Mock
    private ActorRepository actorRepository;

    @InjectMocks
    private ActorServiceImpl actorService;

    private ActorRequest validRequest;
    private Actor savedActor;

    @BeforeEach
    void setUp() {
        validRequest = ActorRequest.builder()
                .firstName("Leonardo")
                .lastName("DiCaprio")
                .email("leo@example.com")
                .nationality("American")
                .build();

        savedActor = Actor.builder()
                .id(1L)
                .firstName("Leonardo")
                .lastName("DiCaprio")
                .email("leo@example.com")
                .nationality("American")
                .build();
    }

    @Test
    @DisplayName("createActor saves and returns response when email is unique")
    void createActor_success() {
        when(actorRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(actorRepository.save(any(Actor.class))).thenReturn(savedActor);

        ActorResponse response = actorService.createActor(validRequest);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("leo@example.com");
        verify(actorRepository).save(any(Actor.class));
    }

    @Test
    @DisplayName("createActor throws DuplicateResourceException when email exists")
    void createActor_duplicateEmail() {
        when(actorRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> actorService.createActor(validRequest))
                .isInstanceOf(DuplicateResourceException.class);

        verify(actorRepository, never()).save(any(Actor.class));
    }

    @ParameterizedTest
    @ValueSource(longs = {99L, 100L, 0L})
    @DisplayName("getActorById throws when actor not found")
    void getActorById_notFound(long id) {
        when(actorRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> actorService.getActorById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getActorById returns actor when found")
    void getActorById_success() {
        when(actorRepository.findById(1L)).thenReturn(Optional.of(savedActor));

        ActorResponse response = actorService.getActorById(1L);

        assertThat(response.getFirstName()).isEqualTo("Leonardo");
    }

    @Test
    @DisplayName("deleteActor throws when actor does not exist")
    void deleteActor_notFound() {
        when(actorRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> actorService.deleteActor(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(actorRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("deleteActor removes actor when it exists")
    void deleteActor_success() {
        when(actorRepository.existsById(1L)).thenReturn(true);

        actorService.deleteActor(1L);

        verify(actorRepository).deleteById(1L);
    }

    @Test
    @DisplayName("updateActor throws when another actor uses the same email")
    void updateActor_duplicateEmail() {
        when(actorRepository.findById(1L)).thenReturn(Optional.of(savedActor));
        when(actorRepository.existsByEmailAndIdNot(anyString(), anyLong())).thenReturn(true);

        assertThatThrownBy(() -> actorService.updateActor(1L, validRequest))
                .isInstanceOf(DuplicateResourceException.class);
    }
}
