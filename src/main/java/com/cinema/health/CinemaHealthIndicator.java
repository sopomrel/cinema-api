package com.cinema.health;

import com.cinema.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CinemaHealthIndicator implements HealthIndicator {

    private final MovieRepository movieRepository;

    @Override
    public Health health() {
        long movieCount = movieRepository.count();
        return Health.up()
                .withDetail("service", "cinema-api")
                .withDetail("moviesInCatalog", movieCount)
                .build();
    }
}
