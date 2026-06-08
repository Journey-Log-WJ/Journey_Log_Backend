package com.wonjun.journeylog.api.series;

import com.wonjun.journeylog.api.series.dto.SeriesDetailResponse;
import com.wonjun.journeylog.api.series.dto.SeriesResponse;
import com.wonjun.journeylog.service.series.SeriesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/series")
@RequiredArgsConstructor
public class SeriesController {

    private final SeriesService seriesService;

    @GetMapping
    public List<SeriesResponse> list() {
        return seriesService.findAll();
    }

    @GetMapping("/{slug}")
    public SeriesDetailResponse detail(@PathVariable String slug) {
        return seriesService.findBySlug(slug);
    }
}
