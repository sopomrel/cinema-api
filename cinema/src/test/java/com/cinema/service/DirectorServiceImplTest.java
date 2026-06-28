package com.cinema.service;

import com.cinema.dto.request.DirectorRequest;
import com.cinema.dto.response.DirectorResponse;
import com.cinema.entity.Director;
import com.cinema.exception.DuplicateResourceException;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.repository.DirectorRepository;
import com.cinema.service.impl.DirectorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
@DisplayName("DirectorService unit tests")
class DirectorServiceImplTest {

    @Mock
    private DirectorRepository directorRepository;

    @InjectMocks
    private DirectorServiceImpl directorService;

    private DirectorRequest validRequest;
    private Director savedDirector;

    @BeforeEach
    void setUp() {
        validRequest = DirectorRequest.builder()
                .firstName("Christopher")
                .lastName("Nolan")
                .email("nolan@example.com")
                .nationality("British")
                .build();

        savedDirector = Director.builder()
                .id(1L)
                .firstName("Christopher")
                .lastName("Nolan")
                .email("nolan@example.com")
                .nationality("British")
                .build();
    }

    @Test
    @DisplayName("createDirector saves and returns response when email is unique")
    void createDirector_success() {
        when(directorRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(directorRepository.save(any(Director.class))).thenReturn(savedDirector);

        DirectorResponse response = directorService.createDirector(validRequest);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("nolan@example.com");
        verify(directorRepository).save(any(Director.class));
    }

    @Test
    @DisplayName("createDirector throws DuplicateResourceException when email exists")
    void createDirector_duplicateEmail() {
        when(directorRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> directorService.createDirector(validRequest))
                .isInstanceOf(DuplicateResourceException.class);

        verify(directorRepository, never()).save(any(Director.class));
    }

    @Test
    @DisplayName("getAllDirectors maps every entity to a response")
    void getAllDirectors_returnsAll() {
        when(directorRepository.findAll()).thenReturn(List.of(savedDirector, savedDirector));

        List<DirectorResponse> directors = directorService.getAllDirectors();

        assertThat(directors).hasSize(2);
    }

    @Test
    @DisplayName("getDirectorById returns director when found")
    void getDirectorById_success() {
        when(directorRepository.findById(1L)).thenReturn(Optional.of(savedDirector));

        DirectorResponse response = directorService.getDirectorById(1L);

        assertThat(response.getLastName()).isEqualTo("Nolan");
    }

    @ParameterizedTest
    @ValueSource(longs = {99L, 100L, 0L})
    @DisplayName("getDirectorById throws when director not found")
    void getDirectorById_notFound(long id) {
        when(directorRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> directorService.getDirectorById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateDirector throws when another director uses the same email")
    void updateDirector_duplicateEmail() {
        when(directorRepository.findById(1L)).thenReturn(Optional.of(savedDirector));
        when(directorRepository.existsByEmailAndIdNot(anyString(), anyLong())).thenReturn(true);

        assertThatThrownBy(() -> directorService.updateDirector(1L, validRequest))
                .isInstanceOf(DuplicateResourceException.class);

        verify(directorRepository, never()).save(any(Director.class));
    }

    @Test
    @DisplayName("updateDirector saves new values when email remains unique")
    void updateDirector_success() {
        when(directorRepository.findById(1L)).thenReturn(Optional.of(savedDirector));
        when(directorRepository.existsByEmailAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(directorRepository.save(any(Director.class))).thenReturn(savedDirector);

        DirectorResponse response = directorService.updateDirector(1L, validRequest);

        assertThat(response.getFirstName()).isEqualTo("Christopher");
        verify(directorRepository).save(any(Director.class));
    }

    @Test
    @DisplayName("deleteDirector throws when director does not exist")
    void deleteDirector_notFound() {
        when(directorRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> directorService.deleteDirector(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(directorRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("deleteDirector removes director when it exists")
    void deleteDirector_success() {
        when(directorRepository.existsById(1L)).thenReturn(true);

        directorService.deleteDirector(1L);

        verify(directorRepository).deleteById(1L);
    }
}
