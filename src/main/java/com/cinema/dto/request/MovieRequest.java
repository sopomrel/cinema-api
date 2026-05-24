package com.cinema.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieRequest {

    @NotNull(message = "Title cannot be null")
    @Size(min = 1, max = 150, message = "Title must be between 1 and 150 characters")
    private String title;

    @NotNull(message = "Release year cannot be null")
    @Min(value = 1888, message = "Release year must be after 1888")
    @Max(value = 2100, message = "Release year must be before 2100")
    private Integer releaseYear;

    @NotNull(message = "Genre cannot be null")
    @Size(min = 2, max = 50, message = "Genre must be between 2 and 50 characters")
    private String genre;

    @NotNull(message = "Rating cannot be null")
    @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "10.0", message = "Rating must be at most 10.0")
    private Double rating;

    @NotNull(message = "Director ID cannot be null")
    private Long directorId;

    // List of actor IDs to attach to this movie (optional)
    private List<Long> actorIds;
}