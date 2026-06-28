package com.cinema.controller;

import com.cinema.config.OpenApiDocumentation;
import com.cinema.dto.request.ActorRequest;
import com.cinema.dto.response.ActorResponse;
import com.cinema.service.ActorService;
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
@RequestMapping("/api/actors")
@RequiredArgsConstructor
@Tag(name = "Actor", description = "Actor management endpoints")
public class ActorController {

    private final ActorService actorService;

    @PostMapping
    @Operation(summary = "Create a new actor", description = "Registers a new actor. Email must be unique.")
    @OpenApiDocumentation.CreatePersonResponses
    public ResponseEntity<ActorResponse> createActor(@Valid @RequestBody ActorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(actorService.createActor(request));
    }

    @GetMapping
    @Operation(summary = "Get all actors", description = "Returns every actor in the system.")
    @ApiResponse(responseCode = "200", description = "List of actors returned successfully")
    public ResponseEntity<List<ActorResponse>> getAllActors() {
        return ResponseEntity.ok(actorService.getAllActors());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an actor by ID", description = "Returns a single actor by primary key.")
    @OpenApiDocumentation.GetByIdResponses
    public ResponseEntity<ActorResponse> getActorById(
            @Parameter(description = "Actor ID", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(actorService.getActorById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an actor by ID", description = "Updates actor details. Email must remain unique.")
    @OpenApiDocumentation.UpdatePersonResponses
    public ResponseEntity<ActorResponse> updateActor(
            @Parameter(description = "Actor ID", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ActorRequest request) {
        return ResponseEntity.ok(actorService.updateActor(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an actor by ID", description = "Permanently removes an actor from the database.")
    @OpenApiDocumentation.DeleteResponses
    public ResponseEntity<Void> deleteActor(
            @Parameter(description = "Actor ID", required = true, example = "1")
            @PathVariable Long id) {
        actorService.deleteActor(id);
        return ResponseEntity.noContent().build();
    }
}
