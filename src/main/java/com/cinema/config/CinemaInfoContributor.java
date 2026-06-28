package com.cinema.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CinemaInfoContributor implements InfoContributor {

    private final AppSettings appSettings;

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("app", Map.of(
                "name", appSettings.getTitle(),
                "contactEmail", appSettings.getContactEmail(),
                "paginationLimit", appSettings.getPaginationLimit(),
                "catalogPublicEnabled", appSettings.isCatalogPublicEnabled()
        ));
    }
}
