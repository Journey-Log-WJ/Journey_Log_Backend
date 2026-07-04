package com.wonjun.journeylog.service.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.wonjun.journeylog.config.GithubProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class GithubContributionsClient {

    private final RestClient restClient;

    public GithubContributionsClient(GithubProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.contributions().apiBaseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public List<DayCount> fetch(String username) {
        if (username == null || username.isBlank()) {
            return Collections.emptyList();
        }

        try {
            JsonNode response = restClient.get()
                    .uri("/{username}?y=last", username)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.has("contributions")) {
                log.warn("No contributions in response for {}", username);
                return Collections.emptyList();
            }

            List<DayCount> days = new ArrayList<>();
            for (JsonNode node : response.get("contributions")) {
                String date = node.path("date").asText(null);
                int count = node.path("count").asInt(0);
                if (date != null) {
                    days.add(new DayCount(LocalDate.parse(date), count));
                }
            }
            return days;
        } catch (Exception e) {
            log.warn("Failed to fetch contributions for {}: {}", username, e.getMessage());
            return Collections.emptyList();
        }
    }

    public record DayCount(LocalDate date, int count) {
    }
}
