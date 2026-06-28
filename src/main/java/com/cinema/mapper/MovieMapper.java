package com.cinema.mapper;

import com.cinema.dto.response.ActorResponse;
import com.cinema.dto.response.MovieResponse;
import com.cinema.entity.Movie;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class MovieMapper {

    private MovieMapper() {
    }

    public static MovieResponse toResponse(Movie movie) {
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
