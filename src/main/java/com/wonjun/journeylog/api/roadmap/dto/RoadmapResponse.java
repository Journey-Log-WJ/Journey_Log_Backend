package com.wonjun.journeylog.api.roadmap.dto;

import com.wonjun.journeylog.domain.roadmap.Roadmap;
import com.wonjun.journeylog.domain.roadmap.RoadmapStatus;

import java.time.LocalDate;

public record RoadmapResponse(
        Long id,
        String slug,
        String title,
        String description,
        RoadmapStatus status,
        LocalDate targetDate,
        Integer sortOrder
) {

    public static RoadmapResponse from(Roadmap roadmap) {
        return new RoadmapResponse(
                roadmap.getId(),
                roadmap.getSlug(),
                roadmap.getTitle(),
                roadmap.getDescription(),
                roadmap.getStatus(),
                roadmap.getTargetDate(),
                roadmap.getSortOrder()
        );
    }
}
