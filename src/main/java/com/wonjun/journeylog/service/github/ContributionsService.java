package com.wonjun.journeylog.service.github;

import com.wonjun.journeylog.api.contributions.dto.ContributionDay;
import com.wonjun.journeylog.api.contributions.dto.ContributionsResponse;
import com.wonjun.journeylog.config.GithubProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class ContributionsService {

    private final GithubProperties properties;
    private final GithubContributionsClient client;

    public ContributionsResponse getCombined() {
        List<String> usernames = properties.contributions().usernames();
        if (usernames == null || usernames.isEmpty()) {
            return new ContributionsResponse(0, List.of());
        }

        Map<LocalDate, Integer> merged = new TreeMap<>();
        for (String username : usernames) {
            for (GithubContributionsClient.DayCount day : client.fetch(username)) {
                merged.merge(day.date(), day.count(), Integer::sum);
            }
        }

        List<ContributionDay> days = merged.entrySet().stream()
                .map(e -> new ContributionDay(e.getKey(), e.getValue()))
                .toList();

        int total = days.stream().mapToInt(ContributionDay::count).sum();
        return new ContributionsResponse(total, days);
    }
}
