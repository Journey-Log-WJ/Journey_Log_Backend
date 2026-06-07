package com.wonjun.journeylog.service.sync;

import com.wonjun.journeylog.domain.roadmap.Roadmap;
import com.wonjun.journeylog.domain.roadmap.RoadmapRepository;
import com.wonjun.journeylog.domain.roadmap.RoadmapStatus;
import com.wonjun.journeylog.service.notion.NotionClient;
import com.wonjun.journeylog.service.notion.NotionRoadmapPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoadmapSyncService {

    private final NotionClient notionClient;
    private final RoadmapRepository roadmapRepository;

    @Transactional
    public RoadmapSyncResult syncFromNotion() {
        List<NotionRoadmapPage> pages = notionClient.queryRoadmapDatabase();

        int created = 0;
        int updated = 0;
        int skipped = 0;

        for (NotionRoadmapPage page : pages) {
            if (!isValid(page)) {
                log.warn("Skipping invalid Notion page {} (missing required fields)", page.notionPageId());
                skipped++;
                continue;
            }

            Optional<Roadmap> existing = roadmapRepository.findByNotionPageId(page.notionPageId());
            if (existing.isEmpty()) {
                roadmapRepository.save(toNewEntity(page));
                created++;
            } else {
                Roadmap roadmap = existing.get();
                if (isUnchanged(roadmap, page)) {
                    continue;
                }
                roadmap.update(
                        page.title(),
                        page.description(),
                        parseStatus(page.status()),
                        page.targetDate(),
                        page.sortOrder(),
                        page.lastEditedTime()
                );
                updated++;
            }
        }

        log.info("Notion sync done — created={}, updated={}, skipped={}", created, updated, skipped);
        return new RoadmapSyncResult(created, updated, skipped);
    }

    private boolean isValid(NotionRoadmapPage page) {
        return page.title() != null && !page.title().isBlank()
                && page.slug() != null && !page.slug().isBlank()
                && page.status() != null && !page.status().isBlank();
    }

    private boolean isUnchanged(Roadmap roadmap, NotionRoadmapPage page) {
        return roadmap.getNotionLastEditedAt() != null
                && !page.lastEditedTime().isAfter(roadmap.getNotionLastEditedAt());
    }

    private Roadmap toNewEntity(NotionRoadmapPage page) {
        return Roadmap.builder()
                .slug(page.slug())
                .title(page.title())
                .description(page.description())
                .status(parseStatus(page.status()))
                .targetDate(page.targetDate())
                .sortOrder(page.sortOrder())
                .notionPageId(page.notionPageId())
                .notionLastEditedAt(page.lastEditedTime())
                .build();
    }

    private RoadmapStatus parseStatus(String raw) {
        return RoadmapStatus.valueOf(raw.trim().toUpperCase());
    }
}
