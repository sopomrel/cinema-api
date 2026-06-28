package com.cinema.service;

import com.cinema.config.AppSettings;
import com.cinema.dto.request.MovieRequest;
import com.cinema.dto.response.MovieResponse;
import com.cinema.entity.Actor;
import com.cinema.entity.Director;
import com.cinema.entity.Movie;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.repository.ActorRepository;
import com.cinema.repository.DirectorRepository;
import com.cinema.repository.MovieRepository;
import com.cinema.service.impl.MovieServiceImpl;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieService unit tests")
class MovieServiceImplTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private DirectorRepository directorRepository;

    @Mock
    private ActorRepository actorRepository;

    @Mock
    private AppSettings appSettings;

    @Spy
    private SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    @InjectMocks
    private MovieServiceImpl movieService;

    private MovieRequest movieRequest;
    private Director director;
    private Movie savedMovie;

    @BeforeEach
    void setUp() {
        director = Director.builder()
                .id(1L)
                .firstName("Christopher")
                .lastName("Nolan")
                .email("nolan@example.com")
                .build();

        movieRequest = MovieRequest.builder()
                .title("Inception")
                .releaseYear(2010)
                .genre("Sci-Fi")
                .rating(8.8)
                .directorId(1L)
                .actorIds(List.of())
                .build();

        savedMovie = Movie.builder()
                .id(1L)
                .title("Inception")
                .releaseYear(2010)
                .genre("Sci-Fi")
                .rating(8.8)
                .director(director)
                .actors(List.of())
                .build();
    }

    @Test
    @DisplayName("createMovie increments custom metric and returns response")
    void createMovie_success() {
        when(directorRepository.findById(1L)).thenReturn(Optional.of(director));
        when(movieRepository.save(any(Movie.class))).thenReturn(savedMovie);

        MovieResponse response = movieService.createMovie(movieRequest);

        assertThat(response.getTitle()).isEqualTo("Inception");
        assertThat(meterRegistry.counter("cinema.movies.created").count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("createMovie throws when director not found")
    void createMovie_directorNotFound() {
        when(directorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.createMovie(movieRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getAllMovies respects pagination limit from AppSettings")
    void getAllMovies_appliesPaginationLimit() {
        when(appSettings.getPaginationLimit()).thenReturn(1);
        when(movieRepository.findAllWithDetails()).thenReturn(List.of(savedMovie, savedMovie));

        List<MovieResponse> movies = movieService.getAllMovies();

        assertThat(movies).hasSize(1);
    }

    @Test
    @DisplayName("deleteMovie throws when movie does not exist")
    void deleteMovie_notFound() {
        when(movieRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> movieService.deleteMovie(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createMovie resolves actors when actor IDs are provided")
    void createMovie_withActors() {
        Actor actor = Actor.builder().id(2L).firstName("Leo").lastName("DiCaprio").email("leo@example.com").build();
        movieRequest.setActorIds(List.of(2L));

        when(directorRepository.findById(1L)).thenReturn(Optional.of(director));
        when(actorRepository.findById(2L)).thenReturn(Optional.of(actor));
        when(movieRepository.save(any(Movie.class))).thenReturn(savedMovie);

        movieService.createMovie(movieRequest);

        verify(actorRepository).findById(2L);
    }
}
