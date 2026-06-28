package com.cinema.controller;

import com.cinema.dto.response.UserInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login status and current user profile")
public class AuthController {

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get current authenticated user",
            description = "Returns the logged-in username and roles. Requires authentication (session or HTTP Basic).",
            security = @SecurityRequirement(name = "basicAuth")
    )
    public ResponseEntity<UserInfoResponse> getCurrentUser(Authentication authentication) {
        log.info("Profile accessed by user {}", authentication.getName());
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return ResponseEntity.ok(UserInfoResponse.builder()
                .username(authentication.getName())
                .roles(roles)
                .authenticated(true)
                .build());
    }
}
