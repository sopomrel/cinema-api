package com.cinema.config;

import com.cinema.entity.AppUser;
import com.cinema.repository.AppUserRepository;
import com.cinema.security.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserDataInitializer implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUser("user", "user123", Set.of(RoleType.USER));
        seedUser("admin", "admin123", Set.of(RoleType.ADMIN, RoleType.USER));
    }

    private void seedUser(String username, String rawPassword, Set<RoleType> roles) {
        if (appUserRepository.existsByUsername(username)) {
            return;
        }
        appUserRepository.save(AppUser.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .enabled(true)
                .roles(roles)
                .build());
    }
}
