package com.wonjun.journeylog.api.contributions.dto;

import java.time.LocalDate;

public record ContributionDay(
        LocalDate date,
        int count
) {
}
