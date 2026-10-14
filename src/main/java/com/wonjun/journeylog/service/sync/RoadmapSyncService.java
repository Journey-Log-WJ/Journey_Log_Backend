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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        int deleted = 0;

        // 노션에 남아 있는 페이지 id (필수값이 빠져 skip 된 페이지도 포함 - 삭제 대상 아님)
        Set<String> liveNotionIds = new HashSet<>();

        for (NotionRoadmapPage page : pages) {
            liveNotionIds.add(page.notionPageId());
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
                        page.slug(),
                        page.title(),
                        page.description(),
                        page.period(),
                        page.story(),
                        parseStatus(page.status()),
                        page.targetDate(),
                        page.sortOrder(),
                        page.lastEditedTime()
                );
                updated++;
            }
        }

        // 노션에서 지운(휴지통) 항목은 DB 에서도 삭제 - 글(Velog) 동기화와 같은 방식
        if (liveNotionIds.isEmpty()) {
            log.warn("Notion returned zero roadmap pages — skipping tombstone cleanup as a safety measure");
        } else {
            for (Roadmap roadmap : roadmapRepository.findAllByNotionPageIdIsNotNull()) {
                if (!liveNotionIds.contains(roadmap.getNotionPageId())) {
                    log.info("Removing roadmap no longer present in Notion: slug={} notionPageId={}",
                            roadmap.getSlug(), roadmap.getNotionPageId());
                    roadmapRepository.delete(roadmap);
                    deleted++;
                }
            }
        }

        log.info("Notion sync done — created={}, updated={}, skipped={}, deleted={}", created, updated, skipped, deleted);
        return new RoadmapSyncResult(created, updated, skipped, deleted);
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
                .period(page.period())
                .story(page.story())
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
