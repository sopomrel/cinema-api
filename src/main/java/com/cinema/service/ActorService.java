package com.cinema.service;

import com.cinema.dto.request.ActorRequest;
import com.cinema.dto.response.ActorResponse;

import java.util.List;

public interface ActorService {

    ActorResponse createActor(ActorRequest request);

    List<ActorResponse> getAllActors();

    ActorResponse getActorById(Long id);

    ActorResponse updateActor(Long id, ActorRequest request);

    void deleteActor(Long id);
}
