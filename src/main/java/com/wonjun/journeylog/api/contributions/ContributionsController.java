package com.wonjun.journeylog.api.contributions;

import com.wonjun.journeylog.api.contributions.dto.ContributionsResponse;
import com.wonjun.journeylog.service.github.ContributionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contributions")
@RequiredArgsConstructor
public class ContributionsController {

    private final ContributionsService contributionsService;

    @GetMapping
    public ContributionsResponse get() {
        return contributionsService.getCombined();
    }
}
