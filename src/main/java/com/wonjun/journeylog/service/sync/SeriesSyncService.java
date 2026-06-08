package com.wonjun.journeylog.service.sync;

import com.wonjun.journeylog.domain.post.Post;
import com.wonjun.journeylog.domain.post.PostRepository;
import com.wonjun.journeylog.domain.series.Series;
import com.wonjun.journeylog.domain.series.SeriesRepository;
import com.wonjun.journeylog.service.velog.VelogClient;
import com.wonjun.journeylog.service.velog.VelogSeriesDetail;
import com.wonjun.journeylog.service.velog.VelogSeriesSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeriesSyncService {

    private final VelogClient velogClient;
    private final SeriesRepository seriesRepository;
    private final PostRepository postRepository;

    @Transactional
    public SeriesSyncResult syncFromVelog() {
        List<VelogSeriesSummary> summaries = velogClient.listSeries();

        int seriesCreated = 0;
        int seriesUpdated = 0;
        int postsLinked = 0;

        for (VelogSeriesSummary summary : summaries) {
            if (!isValid(summary)) {
                log.warn("Skipping invalid Velog series summary (id={})", summary.id());
                continue;
            }

            Optional<Series> existing = seriesRepository.findByVelogSeriesId(summary.id());
            Series series;
            if (existing.isEmpty()) {
                series = Series.builder()
                        .velogSeriesId(summary.id())
                        .slug(summary.urlSlug())
                        .name(summary.name())
                        .description(summary.description())
                        .postsCount(summary.postsCount())
                        .velogUpdatedAt(summary.updatedAt())
                        .build();
                seriesRepository.save(series);
                seriesCreated++;
            } else {
                series = existing.get();
                series.updateFromVelog(
                        summary.name(),
                        summary.description(),
                        summary.postsCount(),
                        summary.updatedAt()
                );
                seriesUpdated++;
            }

            VelogSeriesDetail detail = velogClient.readSeries(summary.id());
            if (detail == null) {
                log.warn("readSeries returned null for id={}", summary.id());
                continue;
            }

            for (VelogSeriesDetail.VelogSeriesPostItem item : detail.seriesPosts()) {
                if (item.postUrlSlug() == null || item.postUrlSlug().isBlank()) continue;
                Optional<Post> post = postRepository.findBySlug(item.postUrlSlug());
                if (post.isEmpty()) {
                    log.debug("Post not found in DB for slug={} (series={})", item.postUrlSlug(), series.getSlug());
                    continue;
                }
                post.get().assignToSeries(series, item.index());
                postsLinked++;
            }
        }

        log.info("Velog series sync done — created={}, updated={}, postsLinked={}",
                seriesCreated, seriesUpdated, postsLinked);
        return new SeriesSyncResult(seriesCreated, seriesUpdated, postsLinked);
    }

    private boolean isValid(VelogSeriesSummary summary) {
        return summary.id() != null && !summary.id().isBlank()
                && summary.urlSlug() != null && !summary.urlSlug().isBlank()
                && summary.name() != null && !summary.name().isBlank();
    }
}
