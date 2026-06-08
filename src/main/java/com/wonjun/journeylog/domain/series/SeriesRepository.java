package com.wonjun.journeylog.domain.series;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeriesRepository extends JpaRepository<Series, Long> {

    Optional<Series> findByVelogSeriesId(String velogSeriesId);

    Optional<Series> findBySlug(String slug);

    List<Series> findAllByOrderByVelogUpdatedAtDesc();
}
