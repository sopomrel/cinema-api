package com.cinema.controller;

import com.cinema.dto.response.AdminSummaryResponse;
import com.cinema.repository.ActorRepository;
import com.cinema.repository.AppUserRepository;
import com.cinema.repository.DirectorRepository;
import com.cinema.repository.MovieRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "ADMIN-only management endpoints")
@SecurityRequirement(name = "basicAuth")
public class AdminController {

    private final MovieRepository movieRepository;
    private final ActorRepository actorRepository;
    private final DirectorRepository directorRepository;
    private final AppUserRepository appUserRepository;

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get system summary", description = "ADMIN only — returns entity counts across the cinema database.")
    public ResponseEntity<AdminSummaryResponse> getSummary() {
        return ResponseEntity.ok(AdminSummaryResponse.builder()
                .movieCount(movieRepository.count())
                .actorCount(actorRepository.count())
                .directorCount(directorRepository.count())
                .userCount(appUserRepository.count())
                .build());
    }
}
