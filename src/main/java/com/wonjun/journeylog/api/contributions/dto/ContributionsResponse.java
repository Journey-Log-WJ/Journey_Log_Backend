package com.wonjun.journeylog.api.contributions.dto;

import java.util.List;

public record ContributionsResponse(
        int totalCount,
        List<ContributionDay> days
) {
}
