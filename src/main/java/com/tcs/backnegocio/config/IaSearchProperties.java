package com.tcs.backnegocio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ia")
public record IaSearchProperties(String search) {

    private static final String DEFAULT_BASE_URL = "http://localhost:8001";

    public String baseUrlOrDefault() {
        if (search == null || search.isBlank()) {
            return DEFAULT_BASE_URL;
        }

        return search.endsWith("/")
                ? search.substring(0, search.length() - 1)
                : search;
    }

    public String searchUrlOrDefault() {
        return baseUrlOrDefault() + "/search";
    }

    public String askChunksUrlOrDefault() {
        return baseUrlOrDefault() + "/ask/chunks";
    }
}
