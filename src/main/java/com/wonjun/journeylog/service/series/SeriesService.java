package com.wonjun.journeylog.service.series;

import com.wonjun.journeylog.api.series.dto.SeriesDetailResponse;
import com.wonjun.journeylog.api.series.dto.SeriesResponse;
import com.wonjun.journeylog.domain.post.Post;
import com.wonjun.journeylog.domain.post.PostRepository;
import com.wonjun.journeylog.domain.series.Series;
import com.wonjun.journeylog.domain.series.SeriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeriesService {

    private final SeriesRepository seriesRepository;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public List<SeriesResponse> findAll() {
        return seriesRepository.findAllByOrderByVelogUpdatedAtDesc().stream()
                .map(SeriesResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SeriesDetailResponse findBySlug(String slug) {
        Series series = seriesRepository.findBySlug(slug)
                .orElseThrow(() -> new SeriesNotFoundException(slug));
        List<Post> posts = postRepository.findAllBySeriesIdOrderBySeriesIndexAsc(series.getId());
        return SeriesDetailResponse.from(series, posts);
    }
}
