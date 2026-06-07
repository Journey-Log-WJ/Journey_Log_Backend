package com.wonjun.journeylog.service.velog;

import java.time.OffsetDateTime;
import java.util.List;

public record VelogPostSummary(
        String id,
        String title,
        String urlSlug,
        OffsetDateTime releasedAt,
        OffsetDateTime updatedAt,
        String shortDescription,
        List<String> tags
) {
}
