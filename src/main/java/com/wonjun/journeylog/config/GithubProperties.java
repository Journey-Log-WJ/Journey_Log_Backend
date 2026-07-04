package com.wonjun.journeylog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "github")
public record GithubProperties(
        Contributions contributions
) {
    public record Contributions(
            String apiBaseUrl,
            List<String> usernames
    ) {
    }
}
