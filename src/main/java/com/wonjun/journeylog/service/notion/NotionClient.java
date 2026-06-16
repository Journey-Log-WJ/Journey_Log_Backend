package com.wonjun.journeylog.service.notion;

import com.fasterxml.jackson.databind.JsonNode;
import com.wonjun.journeylog.config.NotionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class NotionClient {

    private final RestClient restClient;
    private final NotionProperties properties;

    public NotionClient(NotionProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.apiBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.token())
                .defaultHeader("Notion-Version", properties.version())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public List<NotionRoadmapPage> queryRoadmapDatabase() {
        String databaseId = properties.roadmapDatabaseId();
        if (databaseId == null || databaseId.isBlank()) {
            log.warn("Notion roadmap database id is not configured");
            return Collections.emptyList();
        }

        JsonNode response = restClient.post()
                .uri("/databases/{id}/query", databaseId)
                .body("{}")
                .retrieve()
                .body(JsonNode.class);

        if (response == null || !response.has("results")) {
            return Collections.emptyList();
        }

        List<NotionRoadmapPage> pages = new ArrayList<>();
        for (JsonNode result : response.get("results")) {
            pages.add(toPage(result));
        }
        return pages;
    }

    private NotionRoadmapPage toPage(JsonNode result) {
        String id = result.path("id").asText();
        OffsetDateTime lastEdited = OffsetDateTime.parse(result.path("last_edited_time").asText());
        JsonNode props = result.path("properties");

        return new NotionRoadmapPage(
                id,
                lastEdited,
                readTitle(props.path("Title")),
                readRichText(props.path("Slug")),
                readRichText(props.path("Description")),
                readRichText(props.path("Period")),
                readRichText(props.path("Story")),
                readSelect(props.path("Status")),
                readDate(props.path("TargetDate")),
                readNumber(props.path("SortOrder"))
        );
    }

    private String readTitle(JsonNode prop) {
        JsonNode arr = prop.path("title");
        if (!arr.isArray() || arr.isEmpty()) {
            return null;
        }
        return arr.get(0).path("plain_text").asText(null);
    }

    private String readRichText(JsonNode prop) {
        JsonNode arr = prop.path("rich_text");
        if (!arr.isArray() || arr.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (JsonNode chunk : arr) {
            sb.append(chunk.path("plain_text").asText(""));
        }
        String result = sb.toString();
        return result.isEmpty() ? null : result;
    }

    private String readSelect(JsonNode prop) {
        JsonNode select = prop.path("select");
        if (select.isMissingNode() || select.isNull()) {
            return null;
        }
        return select.path("name").asText(null);
    }

    private LocalDate readDate(JsonNode prop) {
        JsonNode date = prop.path("date");
        if (date.isMissingNode() || date.isNull()) {
            return null;
        }
        String start = date.path("start").asText(null);
        return start == null ? null : LocalDate.parse(start.substring(0, 10));
    }

    private Integer readNumber(JsonNode prop) {
        JsonNode num = prop.path("number");
        if (num.isMissingNode() || num.isNull()) {
            return null;
        }
        return num.asInt();
    }
}
