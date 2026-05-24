package com.cinema.service.impl;

import com.cinema.dto.request.ActorRequest;
import com.cinema.dto.response.ActorResponse;
import com.cinema.entity.Actor;
import com.cinema.exception.DuplicateResourceException;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.mapper.ActorMapper;
import com.cinema.repository.ActorRepository;
import com.cinema.service.ActorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ActorServiceImpl implements ActorService {

    private final ActorRepository actorRepository;

    @Override
    public ActorResponse createActor(ActorRequest request) {
        if (actorRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Actor with email already exists: " + request.getEmail());
        }

        Actor actor = Actor.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .nationality(request.getNationality())
                .build();

        return ActorMapper.toResponse(actorRepository.save(actor));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActorResponse> getAllActors() {
        return actorRepository.findAll()
                .stream()
                .map(ActorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ActorResponse getActorById(Long id) {
        Actor actor = actorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found with ID: " + id));
        return ActorMapper.toResponse(actor);
    }

    @Override
    public ActorResponse updateActor(Long id, ActorRequest request) {
        Actor actor = actorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found with ID: " + id));

        if (actorRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new DuplicateResourceException("Actor with email already exists: " + request.getEmail());
        }

        actor.setFirstName(request.getFirstName());
        actor.setLastName(request.getLastName());
        actor.setEmail(request.getEmail());
        actor.setNationality(request.getNationality());

        return ActorMapper.toResponse(actorRepository.save(actor));
    }

    @Override
    public void deleteActor(Long id) {
        if (!actorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Actor not found with ID: " + id);
        }
        actorRepository.deleteById(id);
    }
}
