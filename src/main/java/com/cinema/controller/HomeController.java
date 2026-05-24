package com.cinema.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
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

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("application", "Cinema API");
        response.put("status", "running");
        response.put("message", "Use the links below to explore the API.");
        response.put("links", links);
        return response;
    }
}
