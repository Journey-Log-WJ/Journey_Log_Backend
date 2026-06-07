package com.wonjun.journeylog.domain.roadmap;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {

    Optional<Roadmap> findBySlug(String slug);

    Optional<Roadmap> findByNotionPageId(String notionPageId);

    List<Roadmap> findAllByOrderBySortOrderAscTargetDateAsc();
}
