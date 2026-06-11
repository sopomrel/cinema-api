package com.cinema.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.settings")
public class AppSettings {

    @NotBlank(message = "Application title must not be blank")
    private String title;

    @NotBlank(message = "Contact email must not be blank")
    @Email(message = "Contact email must be a valid email address")
    private String contactEmail;

    @Min(value = 1, message = "Pagination limit must be at least 1")
    @Max(value = 100, message = "Pagination limit must not exceed 100")
    private int paginationLimit;

    @NotBlank(message = "External service URL must not be blank")
    private String externalServiceUrl;

    private boolean catalogPublicEnabled = true;
}
