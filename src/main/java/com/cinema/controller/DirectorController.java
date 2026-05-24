package com.cinema.controller;

import com.cinema.config.OpenApiDocumentation;
import com.cinema.dto.request.DirectorRequest;
import com.cinema.dto.response.DirectorResponse;
import com.cinema.service.DirectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directors")
@RequiredArgsConstructor
@Tag(name = "Director", description = "Director management endpoints")
public class DirectorController {

    private final DirectorService directorService;

    @PostMapping
    @Operation(summary = "Create a new director", description = "Registers a new director. Email must be unique.")
    @OpenApiDocumentation.CreatePersonResponses
    public ResponseEntity<DirectorResponse> createDirector(@Valid @RequestBody DirectorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(directorService.createDirector(request));
    }

    @GetMapping
    @Operation(summary = "Get all directors", description = "Returns every director in the system.")
    @ApiResponse(responseCode = "200", description = "List of directors returned successfully")
    public ResponseEntity<List<DirectorResponse>> getAllDirectors() {
        return ResponseEntity.ok(directorService.getAllDirectors());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a director by ID", description = "Returns a single director by primary key.")
    @OpenApiDocumentation.GetByIdResponses
    public ResponseEntity<DirectorResponse> getDirectorById(
            @Parameter(description = "Director ID", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(directorService.getDirectorById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a director by ID", description = "Updates director details. Email must remain unique.")
    @OpenApiDocumentation.UpdatePersonResponses
    public ResponseEntity<DirectorResponse> updateDirector(
            @Parameter(description = "Director ID", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody DirectorRequest request) {
        return ResponseEntity.ok(directorService.updateDirector(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a director by ID", description = "Permanently removes a director from the database.")
    @OpenApiDocumentation.DeleteResponses
    public ResponseEntity<Void> deleteDirector(
            @Parameter(description = "Director ID", required = true, example = "1")
            @PathVariable Long id) {
        directorService.deleteDirector(id);
        return ResponseEntity.noContent().build();
    }
}
