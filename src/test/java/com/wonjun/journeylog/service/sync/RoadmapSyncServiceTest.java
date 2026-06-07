package com.wonjun.journeylog.service.sync;

import com.wonjun.journeylog.domain.roadmap.Roadmap;
import com.wonjun.journeylog.domain.roadmap.RoadmapRepository;
import com.wonjun.journeylog.domain.roadmap.RoadmapStatus;
import com.wonjun.journeylog.service.notion.NotionClient;
import com.wonjun.journeylog.service.notion.NotionRoadmapPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoadmapSyncServiceTest {

    @Mock
    private NotionClient notionClient;

    @Mock
    private RoadmapRepository roadmapRepository;

    @InjectMocks
    private RoadmapSyncService syncService;

    private NotionRoadmapPage validPage(String pageId, String slug, OffsetDateTime lastEdited) {
        return new NotionRoadmapPage(
                pageId,
                lastEdited,
                "MVP 배포",
                slug,
                "1차 배포",
                "planning",
                LocalDate.of(2026, 8, 1),
                1
        );
    }

    @BeforeEach
    void setUp() {
        given(notionClient.queryRoadmapDatabase()).willReturn(List.of());
    }

    @Test
    void 새_노션_페이지는_create로_저장된다() {
        OffsetDateTime now = OffsetDateTime.now();
        NotionRoadmapPage page = validPage("page-1", "ship-mvp", now);
        given(notionClient.queryRoadmapDatabase()).willReturn(List.of(page));
        given(roadmapRepository.findByNotionPageId("page-1")).willReturn(Optional.empty());

        RoadmapSyncResult result = syncService.syncFromNotion();

        ArgumentCaptor<Roadmap> captor = ArgumentCaptor.forClass(Roadmap.class);
        verify(roadmapRepository).save(captor.capture());
        Roadmap saved = captor.getValue();

        assertThat(saved.getSlug()).isEqualTo("ship-mvp");
        assertThat(saved.getTitle()).isEqualTo("MVP 배포");
        assertThat(saved.getStatus()).isEqualTo(RoadmapStatus.PLANNING);
        assertThat(saved.getNotionPageId()).isEqualTo("page-1");
        assertThat(saved.getNotionLastEditedAt()).isEqualTo(now);
        assertThat(result).isEqualTo(new RoadmapSyncResult(1, 0, 0));
    }

    @Test
    void 노션이_더_최신이면_기존_엔티티가_update된다() {
        OffsetDateTime oldTime = OffsetDateTime.now().minusHours(1);
        OffsetDateTime newTime = OffsetDateTime.now();

        Roadmap existing = Roadmap.builder()
                .slug("ship-mvp")
                .title("(이전 제목)")
                .status(RoadmapStatus.PLANNING)
                .notionPageId("page-1")
                .notionLastEditedAt(oldTime)
                .build();

        NotionRoadmapPage page = new NotionRoadmapPage(
                "page-1", newTime, "MVP 배포 (수정)", "ship-mvp",
                "수정됨", "doing", LocalDate.of(2026, 9, 1), 2);

        given(notionClient.queryRoadmapDatabase()).willReturn(List.of(page));
        given(roadmapRepository.findByNotionPageId("page-1")).willReturn(Optional.of(existing));

        RoadmapSyncResult result = syncService.syncFromNotion();

        assertThat(existing.getTitle()).isEqualTo("MVP 배포 (수정)");
        assertThat(existing.getStatus()).isEqualTo(RoadmapStatus.DOING);
        assertThat(existing.getSortOrder()).isEqualTo(2);
        assertThat(existing.getNotionLastEditedAt()).isEqualTo(newTime);
        verify(roadmapRepository, never()).save(any());
        assertThat(result).isEqualTo(new RoadmapSyncResult(0, 1, 0));
    }

    @Test
    void 노션_수정시각이_같거나_이전이면_skip된다() {
        OffsetDateTime savedTime = OffsetDateTime.now();

        Roadmap existing = Roadmap.builder()
                .slug("ship-mvp").title("MVP")
                .status(RoadmapStatus.PLANNING)
                .notionPageId("page-1")
                .notionLastEditedAt(savedTime)
                .build();

        NotionRoadmapPage page = validPage("page-1", "ship-mvp", savedTime);

        given(notionClient.queryRoadmapDatabase()).willReturn(List.of(page));
        given(roadmapRepository.findByNotionPageId("page-1")).willReturn(Optional.of(existing));

        RoadmapSyncResult result = syncService.syncFromNotion();

        verify(roadmapRepository, never()).save(any());
        assertThat(result).isEqualTo(new RoadmapSyncResult(0, 0, 0));
    }

    @Test
    void 필수_필드가_없으면_skip되고_skipped_카운트가_증가한다() {
        NotionRoadmapPage missingSlug = new NotionRoadmapPage(
                "page-1", OffsetDateTime.now(),
                "title", null, "desc", "planning", null, 1);

        given(notionClient.queryRoadmapDatabase()).willReturn(List.of(missingSlug));

        RoadmapSyncResult result = syncService.syncFromNotion();

        verify(roadmapRepository, never()).save(any());
        assertThat(result).isEqualTo(new RoadmapSyncResult(0, 0, 1));
    }
}
