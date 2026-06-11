package com.cinema.service.impl;

import com.cinema.dto.request.DirectorRequest;
import com.cinema.dto.response.DirectorResponse;
import com.cinema.entity.Director;
import com.cinema.exception.DuplicateResourceException;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.mapper.DirectorMapper;
import com.cinema.repository.DirectorRepository;
import com.cinema.service.DirectorService;
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
public class DirectorServiceImpl implements DirectorService {

    private final DirectorRepository directorRepository;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public DirectorResponse createDirector(DirectorRequest request) {
        if (directorRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("error.director.duplicateEmail", request.getEmail());
        }

        Director director = Director.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .nationality(request.getNationality())
                .build();

        Director saved = directorRepository.save(director);
        log.info("Director created: id={}, name={} {}", saved.getId(), saved.getFirstName(), saved.getLastName());
        return DirectorMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DirectorResponse> getAllDirectors() {
        return directorRepository.findAll()
                .stream()
                .map(DirectorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DirectorResponse getDirectorById(Long id) {
        Director director = directorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.director.notFound", id));
        return DirectorMapper.toResponse(director);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public DirectorResponse updateDirector(Long id, DirectorRequest request) {
        Director director = directorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.director.notFound", id));

        if (directorRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new DuplicateResourceException("error.director.duplicateEmail", request.getEmail());
        }

        director.setFirstName(request.getFirstName());
        director.setLastName(request.getLastName());
        director.setEmail(request.getEmail());
        director.setNationality(request.getNationality());

        return DirectorMapper.toResponse(directorRepository.save(director));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteDirector(Long id) {
        if (!directorRepository.existsById(id)) {
            throw new ResourceNotFoundException("error.director.notFound", id);
        }
        directorRepository.deleteById(id);
        log.info("Director deleted: id={}", id);
    }
}
