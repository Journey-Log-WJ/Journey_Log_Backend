package com.wonjun.journeylog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notion")
public record NotionProperties(
        String apiBaseUrl,
        String version,
        String token,
        String roadmapDatabaseId
) {
}
