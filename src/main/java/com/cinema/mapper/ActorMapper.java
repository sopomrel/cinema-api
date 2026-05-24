package com.cinema.mapper;

import com.cinema.dto.response.ActorResponse;
import com.cinema.entity.Actor;

public final class ActorMapper {

    private ActorMapper() {
    }

    public static ActorResponse toResponse(Actor actor) {
        int movieCount = actor.getMovies() == null ? 0 : actor.getMovies().size();
        return ActorResponse.builder()
                .id(actor.getId())
                .firstName(actor.getFirstName())
                .lastName(actor.getLastName())
                .email(actor.getEmail())
                .nationality(actor.getNationality())
                .appearedInMovieCount(movieCount)
                .build();
    }
}
