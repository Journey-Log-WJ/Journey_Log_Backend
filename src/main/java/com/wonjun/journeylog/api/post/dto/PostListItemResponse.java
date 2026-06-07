package com.wonjun.journeylog.api.post.dto;

import com.wonjun.journeylog.domain.post.Post;

import java.time.OffsetDateTime;
import java.util.List;

public record PostListItemResponse(
        Long id,
        String slug,
        String title,
        String excerpt,
        OffsetDateTime publishedAt,
        List<TagResponse> tags
) {

    public static PostListItemResponse from(Post post) {
        List<TagResponse> tags = post.getTags().stream()
                .map(TagResponse::from)
                .sorted((a, b) -> a.name().compareTo(b.name()))
                .toList();
        return new PostListItemResponse(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                post.getPublishedAt(),
                tags
        );
    }
}
