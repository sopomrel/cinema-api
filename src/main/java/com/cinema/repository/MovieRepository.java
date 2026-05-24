package com.cinema.repository;

import com.cinema.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    // Get all movies by a specific director
    List<Movie> findByDirectorId(Long directorId);

    // Get all movies by genre
    List<Movie> findByGenreIgnoreCase(String genre);

    // Get all movies by release year
    List<Movie> findByReleaseYear(Integer releaseYear);
}