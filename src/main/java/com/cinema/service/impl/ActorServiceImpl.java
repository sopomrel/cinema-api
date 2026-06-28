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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ActorServiceImpl implements ActorService {

    private final ActorRepository actorRepository;

    @Override
    @PreAuthorize("isAuthenticated()")
    public ActorResponse createActor(ActorRequest request) {
        if (actorRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("error.actor.duplicateEmail", request.getEmail());
        }

        Actor actor = Actor.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .nationality(request.getNationality())
                .build();

        Actor saved = actorRepository.save(actor);
        log.info("Actor created: id={}, name={} {}", saved.getId(), saved.getFirstName(), saved.getLastName());
        return ActorMapper.toResponse(saved);
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
                .orElseThrow(() -> new ResourceNotFoundException("error.actor.notFound", id));
        return ActorMapper.toResponse(actor);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public ActorResponse updateActor(Long id, ActorRequest request) {
        Actor actor = actorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.actor.notFound", id));

        if (actorRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new DuplicateResourceException("error.actor.duplicateEmail", request.getEmail());
        }

        actor.setFirstName(request.getFirstName());
        actor.setLastName(request.getLastName());
        actor.setEmail(request.getEmail());
        actor.setNationality(request.getNationality());

        return ActorMapper.toResponse(actorRepository.save(actor));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteActor(Long id) {
        if (!actorRepository.existsById(id)) {
            throw new ResourceNotFoundException("error.actor.notFound", id);
        }
        actorRepository.deleteById(id);
        log.info("Actor deleted: id={}", id);
    }
}
