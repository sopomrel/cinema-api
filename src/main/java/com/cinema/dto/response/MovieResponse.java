package com.cinema.dto.response;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieResponse {
    private Long id;
    private String title;
    private Integer releaseYear;
    private String genre;
    private Double rating;
    private Long directorId;
    private String directorFirstName;
    private String directorLastName;
    private List<ActorResponse> actors;
}