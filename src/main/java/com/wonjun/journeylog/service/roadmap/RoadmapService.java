package com.wonjun.journeylog.service.roadmap;

import com.wonjun.journeylog.api.roadmap.dto.RoadmapResponse;
import com.wonjun.journeylog.domain.roadmap.RoadmapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;

    public List<RoadmapResponse> findAll() {
        return roadmapRepository.findAllByOrderBySortOrderAscTargetDateAsc().stream()
                .map(RoadmapResponse::from)
                .toList();
    }
}
