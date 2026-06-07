package com.wonjun.journeylog.api.roadmap;

import com.wonjun.journeylog.api.roadmap.dto.RoadmapResponse;
import com.wonjun.journeylog.service.roadmap.RoadmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roadmaps")
@RequiredArgsConstructor
public class RoadmapController {

    private final RoadmapService roadmapService;

    @GetMapping
    public List<RoadmapResponse> list() {
        return roadmapService.findAll();
    }
}
