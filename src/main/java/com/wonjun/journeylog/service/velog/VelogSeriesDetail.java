package com.wonjun.journeylog.service.velog;

import java.time.OffsetDateTime;
import java.util.List;

public record VelogSeriesDetail(
        String id,
        String name,
        String urlSlug,
        String description,
        int postsCount,
        OffsetDateTime updatedAt,
        List<VelogSeriesPostItem> seriesPosts
) {

    public record VelogSeriesPostItem(
            int index,
            String postId,
            String postUrlSlug,
            String postTitle
    ) {
    }
}
