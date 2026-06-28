package com.cinema.mapper;

import com.cinema.dto.response.DirectorResponse;
import com.cinema.entity.Director;

public final class DirectorMapper {

    private DirectorMapper() {
    }

    public static DirectorResponse toResponse(Director director) {
        int movieCount = director.getMovies() == null ? 0 : director.getMovies().size();
        return DirectorResponse.builder()
                .id(director.getId())
                .firstName(director.getFirstName())
                .lastName(director.getLastName())
                .email(director.getEmail())
                .nationality(director.getNationality())
                .directedMovieCount(movieCount)
                .build();
    }
}
