package com.wonjun.journeylog.api.series.dto;

import com.wonjun.journeylog.domain.series.Series;

import java.time.OffsetDateTime;

public record SeriesResponse(
        Long id,
        String slug,
        String name,
        String description,
        int postsCount,
        OffsetDateTime updatedAt
) {

    public static SeriesResponse from(Series series) {
        return new SeriesResponse(
                series.getId(),
                series.getSlug(),
                series.getName(),
                series.getDescription(),
                series.getPostsCount(),
                series.getVelogUpdatedAt()
        );
    }
}
