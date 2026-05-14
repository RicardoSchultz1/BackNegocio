package com.tcs.backnegocio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ia.search")
public record IaSearchProperties(String url) {

    public String urlOrDefault() {
        if (url == null || url.isBlank()) {
            return "http://localhost:8001/search";
        }
        return url;
    }
}
