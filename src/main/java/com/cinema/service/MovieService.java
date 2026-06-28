package com.cinema.service;

import com.cinema.dto.request.MovieRequest;
import com.cinema.dto.response.MovieResponse;

import java.util.List;

public interface MovieService {

    MovieResponse createMovie(MovieRequest request);

    List<MovieResponse> getAllMovies();

    MovieResponse getMovieById(Long id);

    List<MovieResponse> getMoviesByDirector(Long directorId);

    List<MovieResponse> getMoviesByGenre(String genre);

    MovieResponse updateMovie(Long id, MovieRequest request);

    void deleteMovie(Long id);
}
