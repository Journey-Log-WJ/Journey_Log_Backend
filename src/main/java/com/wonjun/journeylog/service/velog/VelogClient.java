package com.wonjun.journeylog.service.velog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonjun.journeylog.config.VelogProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class VelogClient {

    private static final String POSTS_QUERY = """
            query Posts($username: String, $limit: Int) {
              posts(username: $username, limit: $limit) {
                id
                title
                url_slug
                released_at
                updated_at
                short_description
                tags
              }
            }
            """;

    private static final String READ_POST_QUERY = """
            query ReadPost($username: String, $url_slug: String) {
              post(username: $username, url_slug: $url_slug) {
                id
                title
                url_slug
                released_at
                updated_at
                short_description
                body
                tags
              }
            }
            """;

    private static final String SERIES_LIST_QUERY = """
            query SeriesList($username: String) {
              seriesList(username: $username) {
                id
                name
                url_slug
                description
                posts_count
                updated_at
              }
            }
            """;

    private static final String SERIES_QUERY = """
            query Series($id: ID) {
              series(id: $id) {
                id
                name
                url_slug
                description
                posts_count
                updated_at
                series_posts {
                  index
                  post {
                    id
                    url_slug
                    title
                  }
                }
              }
            }
            """;

    private final RestClient restClient;
    private final VelogProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public VelogClient(VelogProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.apiUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    private JsonNode postGraphql(Map<String, Object> body) {
        byte[] raw = restClient.post().body(body).retrieve().body(byte[].class);
        if (raw == null || raw.length == 0) {
            return null;
        }
        String json = new String(raw, StandardCharsets.UTF_8);
        try {
            return objectMapper.readTree(json);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<VelogPostSummary> listPosts() {
        String username = properties.username();
        if (username == null || username.isBlank()) {
            log.warn("Velog username is not configured");
            return Collections.emptyList();
        }

        Map<String, Object> body = Map.of(
                "query", POSTS_QUERY,
                "variables", Map.of("username", username, "limit", properties.pageSize())
        );

        JsonNode response = postGraphql(body);
        if (response == null) {
            return Collections.emptyList();
        }
        JsonNode posts = response.path("data").path("posts");
        if (!posts.isArray()) {
            return Collections.emptyList();
        }

        List<VelogPostSummary> result = new ArrayList<>();
        for (JsonNode node : posts) {
            result.add(toSummary(node));
        }
        return result;
    }

    public VelogPostDetail readPost(String urlSlug) {
        Map<String, Object> body = Map.of(
                "query", READ_POST_QUERY,
                "variables", Map.of(
                        "username", properties.username(),
                        "url_slug", urlSlug)
        );

        JsonNode response = postGraphql(body);
        if (response == null) {
            return null;
        }
        JsonNode post = response.path("data").path("post");
        if (post.isMissingNode() || post.isNull()) {
            return null;
        }
        return toDetail(post);
    }

    public List<VelogSeriesSummary> listSeries() {
        String username = properties.username();
        if (username == null || username.isBlank()) {
            log.warn("Velog username is not configured");
            return Collections.emptyList();
        }

        Map<String, Object> body = Map.of(
                "query", SERIES_LIST_QUERY,
                "variables", Map.of("username", username)
        );

        JsonNode response = postGraphql(body);
        if (response == null) {
            return Collections.emptyList();
        }
        JsonNode seriesList = response.path("data").path("seriesList");
        if (!seriesList.isArray()) {
            return Collections.emptyList();
        }

        List<VelogSeriesSummary> result = new ArrayList<>();
        for (JsonNode node : seriesList) {
            result.add(toSeriesSummary(node));
        }
        return result;
    }

    public VelogSeriesDetail readSeries(String id) {
        Map<String, Object> body = Map.of(
                "query", SERIES_QUERY,
                "variables", Map.of("id", id)
        );

        JsonNode response = postGraphql(body);
        if (response == null) {
            return null;
        }
        JsonNode series = response.path("data").path("series");
        if (series.isMissingNode() || series.isNull()) {
            return null;
        }
        return toSeriesDetail(series);
    }

    private VelogPostSummary toSummary(JsonNode node) {
        return new VelogPostSummary(
                node.path("id").asText(),
                node.path("title").asText(null),
                node.path("url_slug").asText(null),
                parseDateTime(node.path("released_at")),
                parseDateTime(node.path("updated_at")),
                node.path("short_description").asText(null),
                readTags(node.path("tags"))
        );
    }

    private VelogPostDetail toDetail(JsonNode node) {
        return new VelogPostDetail(
                node.path("id").asText(),
                node.path("title").asText(null),
                node.path("url_slug").asText(null),
                parseDateTime(node.path("released_at")),
                parseDateTime(node.path("updated_at")),
                node.path("short_description").asText(null),
                node.path("body").asText(null),
                readTags(node.path("tags"))
        );
    }

    private VelogSeriesSummary toSeriesSummary(JsonNode node) {
        return new VelogSeriesSummary(
                node.path("id").asText(),
                node.path("name").asText(null),
                node.path("url_slug").asText(null),
                safeText(node.path("description")),
                node.path("posts_count").asInt(0),
                parseDateTime(node.path("updated_at"))
        );
    }

    private VelogSeriesDetail toSeriesDetail(JsonNode node) {
        JsonNode seriesPostsNode = node.path("series_posts");
        List<VelogSeriesDetail.VelogSeriesPostItem> items = new ArrayList<>();
        if (seriesPostsNode.isArray()) {
            for (JsonNode item : seriesPostsNode) {
                JsonNode post = item.path("post");
                items.add(new VelogSeriesDetail.VelogSeriesPostItem(
                        item.path("index").asInt(0),
                        post.path("id").asText(null),
                        post.path("url_slug").asText(null),
                        post.path("title").asText(null)
                ));
            }
        }
        return new VelogSeriesDetail(
                node.path("id").asText(),
                node.path("name").asText(null),
                node.path("url_slug").asText(null),
                safeText(node.path("description")),
                node.path("posts_count").asInt(0),
                parseDateTime(node.path("updated_at")),
                items
        );
    }

    private String safeText(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) return null;
        String text = node.asText(null);
        return (text == null || text.isBlank()) ? null : text;
    }

    private List<String> readTags(JsonNode tagsNode) {
        if (!tagsNode.isArray()) {
            return List.of();
        }
        List<String> tags = new ArrayList<>();
        for (JsonNode tag : tagsNode) {
            String value = tag.asText(null);
            if (value != null && !value.isBlank()) {
                tags.add(value);
            }
        }
        return tags;
    }

    private OffsetDateTime parseDateTime(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        String text = node.asText(null);
        if (text == null || text.isBlank()) {
            return null;
        }
        return OffsetDateTime.parse(text);
    }
}
