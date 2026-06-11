package com.cinema.config;

import com.cinema.security.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cinema.exception.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;
    private final AppSettings appSettings;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF is disabled because this is a REST API consumed by Postman/Swagger/curl
                // using session cookies or HTTP Basic, not browser HTML forms.
                // Re-enable CSRF if you add server-rendered Thymeleaf forms to this project.
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .userDetailsService(userDetailsService)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.GET, "/").permitAll()
                            .requestMatchers("/error").permitAll()
                            .requestMatchers("/h2-console/**").permitAll()
                            .requestMatchers(
                                    "/swagger-ui/**",
                                    "/swagger-ui.html",
                                    "/v3/api-docs/**",
                                    "/api-docs/**"
                            ).permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll();

                    if (appSettings.isCatalogPublicEnabled()) {
                        auth.requestMatchers(HttpMethod.GET, "/api/movies/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/actors/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/directors/**").permitAll();
                    } else {
                        auth.requestMatchers(HttpMethod.GET, "/api/movies/**").authenticated()
                                .requestMatchers(HttpMethod.GET, "/api/actors/**").authenticated()
                                .requestMatchers(HttpMethod.GET, "/api/directors/**").authenticated();
                    }

                    auth.requestMatchers("/api/admin/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.POST, "/api/directors/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/api/directors/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/api/directors/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                            .requestMatchers("/api/auth/me").authenticated()
                            .requestMatchers(HttpMethod.POST, "/api/movies/**").authenticated()
                            .requestMatchers(HttpMethod.PUT, "/api/movies/**").authenticated()
                            .requestMatchers(HttpMethod.POST, "/api/actors/**").authenticated()
                            .requestMatchers(HttpMethod.PUT, "/api/actors/**").authenticated()
                            .anyRequest().authenticated();
                })
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            String message = messageSource.getMessage(
                                    "error.access.denied", null, request.getLocale());
                            ErrorResponse body = ErrorResponse.builder()
                                    .status(HttpStatus.FORBIDDEN.value())
                                    .message(message)
                                    .timestamp(LocalDateTime.now())
                                    .build();
                            objectMapper.writeValue(response.getOutputStream(), body);
                        })
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) -> {
                            response.setStatus(HttpStatus.OK.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            objectMapper.writeValue(response.getOutputStream(),
                                    new LoginResponse(
                                            messageSource.getMessage("auth.login.success", null, request.getLocale()),
                                            authentication.getName()));
                        })
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            objectMapper.writeValue(response.getOutputStream(),
                                    new LoginResponse(
                                            messageSource.getMessage("auth.login.failure", null, request.getLocale()),
                                            null));
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpStatus.NO_CONTENT.value()))
                        .permitAll()
                )
                .httpBasic(basic -> {});

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    private record LoginResponse(String message, String username) {
    }
}
