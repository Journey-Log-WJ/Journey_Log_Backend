package com.wonjun.journeylog.service.velog;

import java.time.OffsetDateTime;

public record VelogSeriesSummary(
        String id,
        String name,
        String urlSlug,
        String description,
        int postsCount,
        OffsetDateTime updatedAt
) {
}
