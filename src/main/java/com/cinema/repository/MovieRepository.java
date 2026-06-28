package com.cinema.repository;

import com.cinema.entity.Movie;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    @EntityGraph(attributePaths = {"director", "actors"})
    @Query("SELECT m FROM Movie m")
    List<Movie> findAllWithDetails();

    @EntityGraph(attributePaths = {"director", "actors"})
    Optional<Movie> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"director", "actors"})
    List<Movie> findByDirectorId(Long directorId);

    @EntityGraph(attributePaths = {"director", "actors"})
    List<Movie> findByGenreIgnoreCase(String genre);

    List<Movie> findByReleaseYear(Integer releaseYear);
}