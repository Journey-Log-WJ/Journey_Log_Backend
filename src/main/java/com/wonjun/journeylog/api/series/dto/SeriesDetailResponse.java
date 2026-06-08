package com.wonjun.journeylog.api.series.dto;

import com.wonjun.journeylog.api.post.dto.TagResponse;
import com.wonjun.journeylog.domain.post.Post;
import com.wonjun.journeylog.domain.series.Series;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

public record SeriesDetailResponse(
        Long id,
        String slug,
        String name,
        String description,
        int postsCount,
        OffsetDateTime updatedAt,
        List<SeriesPostItem> posts
) {

    public static SeriesDetailResponse from(Series series, List<Post> posts) {
        List<SeriesPostItem> items = posts.stream()
                .sorted(Comparator.comparing(Post::getSeriesIndex, Comparator.nullsLast(Integer::compareTo)))
                .map(SeriesPostItem::from)
                .toList();
        return new SeriesDetailResponse(
                series.getId(),
                series.getSlug(),
                series.getName(),
                series.getDescription(),
                series.getPostsCount(),
                series.getVelogUpdatedAt(),
                items
        );
    }

    public record SeriesPostItem(
            Long id,
            String slug,
            String title,
            String excerpt,
            OffsetDateTime publishedAt,
            Integer seriesIndex,
            List<TagResponse> tags
    ) {

        public static SeriesPostItem from(Post post) {
            List<TagResponse> tags = post.getTags().stream()
                    .map(TagResponse::from)
                    .sorted((a, b) -> a.name().compareTo(b.name()))
                    .toList();
            return new SeriesPostItem(
                    post.getId(),
                    post.getSlug(),
                    post.getTitle(),
                    post.getExcerpt(),
                    post.getPublishedAt(),
                    post.getSeriesIndex(),
                    tags
            );
        }
    }
}
