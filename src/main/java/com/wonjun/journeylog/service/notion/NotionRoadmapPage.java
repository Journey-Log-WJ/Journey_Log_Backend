package com.wonjun.journeylog.service.notion;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record NotionRoadmapPage(
        String notionPageId,
        OffsetDateTime lastEditedTime,
        String title,
        String slug,
        String description,
        String status,
        LocalDate targetDate,
        Integer sortOrder
) {
}
