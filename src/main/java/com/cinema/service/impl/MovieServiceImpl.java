package com.cinema.service.impl;

import com.cinema.dto.request.MovieRequest;
import com.cinema.dto.response.ActorResponse;
import com.cinema.dto.response.MovieResponse;
import com.cinema.entity.Actor;
import com.cinema.entity.Director;
import com.cinema.entity.Movie;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.mapper.ActorMapper;
import com.cinema.repository.ActorRepository;
import com.cinema.repository.DirectorRepository;
import com.cinema.repository.MovieRepository;
import com.cinema.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final DirectorRepository directorRepository;
    private final ActorRepository actorRepository;

    @Override
    @PreAuthorize("isAuthenticated()")
    public MovieResponse createMovie(MovieRequest request) {
        Director director = directorRepository.findById(request.getDirectorId())
                .orElseThrow(() -> new ResourceNotFoundException("Director not found with ID: " + request.getDirectorId()));

        List<Actor> actors = resolveActors(request.getActorIds());

        Movie movie = Movie.builder()
                .title(request.getTitle())
                .releaseYear(request.getReleaseYear())
                .genre(request.getGenre())
                .rating(request.getRating())
                .director(director)
                .actors(actors)
                .build();

        return mapToResponse(movieRepository.save(movie));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with ID: " + id));
        return mapToResponse(movie);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieResponse> getMoviesByDirector(Long directorId) {
        if (!directorRepository.existsById(directorId)) {
            throw new ResourceNotFoundException("Director not found with ID: " + directorId);
        }
        return movieRepository.findByDirectorId(directorId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieResponse> getMoviesByGenre(String genre) {
        return movieRepository.findByGenreIgnoreCase(genre)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public MovieResponse updateMovie(Long id, MovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with ID: " + id));

        Director director = directorRepository.findById(request.getDirectorId())
                .orElseThrow(() -> new ResourceNotFoundException("Director not found with ID: " + request.getDirectorId()));

        movie.setTitle(request.getTitle());
        movie.setReleaseYear(request.getReleaseYear());
        movie.setGenre(request.getGenre());
        movie.setRating(request.getRating());
        movie.setDirector(director);
        movie.setActors(resolveActors(request.getActorIds()));

        return mapToResponse(movieRepository.save(movie));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Movie not found with ID: " + id);
        }
        movieRepository.deleteById(id);
    }

    private List<Actor> resolveActors(List<Long> actorIds) {
        if (actorIds == null || actorIds.isEmpty()) {
            return new ArrayList<>();
        }
        return actorIds.stream()
                .map(actorId -> actorRepository.findById(actorId)
                        .orElseThrow(() -> new ResourceNotFoundException("Actor not found with ID: " + actorId)))
                .collect(Collectors.toList());
    }

    private MovieResponse mapToResponse(Movie movie) {
        List<ActorResponse> actorResponses = movie.getActors() == null ? new ArrayList<>() :
                movie.getActors().stream()
                        .map(ActorMapper::toResponse)
                        .collect(Collectors.toList());

        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .releaseYear(movie.getReleaseYear())
                .genre(movie.getGenre())
                .rating(movie.getRating())
                .directorId(movie.getDirector().getId())
                .directorFirstName(movie.getDirector().getFirstName())
                .directorLastName(movie.getDirector().getLastName())
                .actors(actorResponses)
                .build();
    }
}
