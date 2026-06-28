package com.cinema.service;

import com.cinema.dto.request.DirectorRequest;
import com.cinema.dto.response.DirectorResponse;

import java.util.List;

public interface DirectorService {

    DirectorResponse createDirector(DirectorRequest request);

    List<DirectorResponse> getAllDirectors();

    DirectorResponse getDirectorById(Long id);

    DirectorResponse updateDirector(Long id, DirectorRequest request);

    void deleteDirector(Long id);
}
