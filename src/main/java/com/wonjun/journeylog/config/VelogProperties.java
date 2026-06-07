package com.wonjun.journeylog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "velog")
public record VelogProperties(
        String apiUrl,
        String username,
        Integer pageSize
) {
}
