package com.cinema.controller;

import com.cinema.config.AppSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HomeController {

    private final AppSettings appSettings;
    private final MessageSource messageSource;

    @GetMapping("/")
    public Map<String, Object> home(Locale locale) {
        log.info("Home endpoint accessed, locale={}", locale);

        Map<String, Object> links = new LinkedHashMap<>();
        links.put("swagger", "/swagger-ui.html");
        links.put("apiDocs", "/api-docs");
        links.put("movies", "/api/movies");
        links.put("actors", "/api/actors");
        links.put("directors", "/api/directors");
        links.put("login", "POST /api/auth/login (form: username, password)");
        links.put("profile", "/api/auth/me (requires authentication)");
        links.put("adminSummary", "/api/admin/summary (ADMIN only)");
        links.put("h2Console", "/h2-console");

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("title", appSettings.getTitle());
        metadata.put("contactEmail", appSettings.getContactEmail());
        metadata.put("paginationLimit", appSettings.getPaginationLimit());
        metadata.put("externalServiceUrl", appSettings.getExternalServiceUrl());
        metadata.put("catalogPublicEnabled", appSettings.isCatalogPublicEnabled());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("application", appSettings.getTitle());
        response.put("status", messageSource.getMessage("app.status.running", null, locale));
        response.put("message", messageSource.getMessage("app.welcome", null, locale));
        response.put("locale", locale.toLanguageTag());
        response.put("metadata", metadata);
        response.put("links", links);
        return response;
    }
}
