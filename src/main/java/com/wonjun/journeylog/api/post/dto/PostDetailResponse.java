package com.wonjun.journeylog.api.post.dto;

import com.wonjun.journeylog.domain.post.Post;

import java.time.OffsetDateTime;
import java.util.List;

public record PostDetailResponse(
        Long id,
        String slug,
        String title,
        String content,
        String excerpt,
        OffsetDateTime publishedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<TagResponse> tags
) {

    public static PostDetailResponse from(Post post) {
        List<TagResponse> tags = post.getTags().stream()
                .map(TagResponse::from)
                .sorted((a, b) -> a.name().compareTo(b.name()))
                .toList();
        return new PostDetailResponse(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getContent(),
                post.getExcerpt(),
                post.getPublishedAt(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                tags
        );
    }
}
