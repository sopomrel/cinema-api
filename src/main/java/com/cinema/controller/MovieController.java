package com.cinema.controller;

import com.cinema.config.OpenApiDocumentation;
import com.cinema.dto.request.MovieRequest;
import com.cinema.dto.response.MovieResponse;
import com.cinema.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Tag(name = "Movie", description = "Movie management endpoints")
public class MovieController {

    private final MovieService movieService;

    @PostMapping
    @Operation(summary = "Create a new movie", description = "Creates a movie linked to a director and optional actors.")
    @OpenApiDocumentation.CreateMovieResponses
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody MovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.createMovie(request));
    }

    @GetMapping
    @Operation(summary = "Get all movies", description = "Returns every movie with director and actor details.")
    @ApiResponse(responseCode = "200", description = "List of movies returned successfully")
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a movie by ID", description = "Returns a single movie by primary key.")
    @OpenApiDocumentation.GetByIdResponses
    public ResponseEntity<MovieResponse> getMovieById(
            @Parameter(description = "Movie ID", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @GetMapping("/director/{directorId}")
    @Operation(summary = "Get all movies by director ID", description = "Filters movies by the given director.")
    @OpenApiDocumentation.GetByIdResponses
    public ResponseEntity<List<MovieResponse>> getMoviesByDirector(
            @Parameter(description = "Director ID", required = true, example = "1")
            @PathVariable Long directorId) {
        return ResponseEntity.ok(movieService.getMoviesByDirector(directorId));
    }

    @GetMapping("/genre/{genre}")
    @Operation(summary = "Get all movies by genre", description = "Case-insensitive genre filter.")
    @ApiResponse(responseCode = "200", description = "Movies matching genre returned successfully")
    public ResponseEntity<List<MovieResponse>> getMoviesByGenre(
            @Parameter(description = "Genre name", required = true, example = "Drama")
            @PathVariable String genre) {
        return ResponseEntity.ok(movieService.getMoviesByGenre(genre));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a movie by ID", description = "Updates movie fields, director, and actor list.")
    @OpenApiDocumentation.UpdateMovieResponses
    public ResponseEntity<MovieResponse> updateMovie(
            @Parameter(description = "Movie ID", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody MovieRequest request) {
        return ResponseEntity.ok(movieService.updateMovie(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a movie by ID", description = "Permanently removes a movie from the database.")
    @OpenApiDocumentation.DeleteResponses
    public ResponseEntity<Void> deleteMovie(
            @Parameter(description = "Movie ID", required = true, example = "1")
            @PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}
