package com.cinema.config;

import com.cinema.entity.Actor;
import com.cinema.entity.Director;
import com.cinema.entity.Movie;
import com.cinema.repository.ActorRepository;
import com.cinema.repository.DirectorRepository;
import com.cinema.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataInitializer implements CommandLineRunner {

    private final DirectorRepository directorRepository;
    private final ActorRepository actorRepository;
    private final MovieRepository movieRepository;

    @Override
    public void run(String... args) {
        if (movieRepository.count() > 0) {
            log.debug("Development data already present, skipping seed");
            return;
        }

        Director director = directorRepository.save(Director.builder()
                .firstName("Christopher")
                .lastName("Nolan")
                .email("nolan@example.com")
                .nationality("British")
                .build());

        Actor actor = actorRepository.save(Actor.builder()
                .firstName("Leonardo")
                .lastName("DiCaprio")
                .email("leo@example.com")
                .nationality("American")
                .build());

        movieRepository.save(Movie.builder()
                .title("Inception")
                .releaseYear(2010)
                .genre("Sci-Fi")
                .rating(8.8)
                .director(director)
                .actors(List.of(actor))
                .build());

        log.info("Development sample data seeded successfully");
    }
}
