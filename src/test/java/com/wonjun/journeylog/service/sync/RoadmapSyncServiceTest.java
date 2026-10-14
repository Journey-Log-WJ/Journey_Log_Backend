package com.wonjun.journeylog.service.sync;

import com.wonjun.journeylog.domain.roadmap.Roadmap;
import com.wonjun.journeylog.domain.roadmap.RoadmapRepository;
import com.wonjun.journeylog.domain.roadmap.RoadmapStatus;
import com.wonjun.journeylog.service.notion.NotionClient;
import com.wonjun.journeylog.service.notion.NotionRoadmapPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private NotionRoadmapPage page(String pageId, String title, String slug, String status, OffsetDateTime lastEdited, int sortOrder) {
        return new NotionRoadmapPage(pageId, lastEdited, title, slug, "설명", "2026.03", null, status, null, sortOrder);
    }

    private Roadmap saved(String pageId, String slug, OffsetDateTime lastEdited) {
        return Roadmap.builder()
                .slug(slug)
                .title("(이전 제목)")
                .status(RoadmapStatus.LEARNED)
                .notionPageId(pageId)
                .notionLastEditedAt(lastEdited)
                .build();
    }

    @Test
    void 새_노션_페이지는_create로_저장된다() {
        OffsetDateTime now = OffsetDateTime.now();
        given(notionClient.queryRoadmapDatabase()).willReturn(List.of(page("page-1", "UXN 합류", "uxn-join", "in_progress", now, 1)));
        given(roadmapRepository.findByNotionPageId("page-1")).willReturn(Optional.empty());

        RoadmapSyncResult result = syncService.syncFromNotion();

        ArgumentCaptor<Roadmap> captor = ArgumentCaptor.forClass(Roadmap.class);
        verify(roadmapRepository).save(captor.capture());
        Roadmap created = captor.getValue();
        assertThat(created.getSlug()).isEqualTo("uxn-join");
        assertThat(created.getTitle()).isEqualTo("UXN 합류");
        assertThat(created.getStatus()).isEqualTo(RoadmapStatus.IN_PROGRESS);
        assertThat(created.getNotionPageId()).isEqualTo("page-1");
        assertThat(created.getNotionLastEditedAt()).isEqualTo(now);
        assertThat(result).isEqualTo(new RoadmapSyncResult(1, 0, 0, 0));
    }

    @Test
    void 노션이_더_최신이면_기존_엔티티가_update된다() {
        OffsetDateTime newTime = OffsetDateTime.now();
        Roadmap existing = saved("page-1", "sqld-pass", newTime.minusHours(1));
        given(notionClient.queryRoadmapDatabase()).willReturn(List.of(page("page-1", "SQLD 취득", "sqld-pass", "succeeded", newTime, 2)));
        given(roadmapRepository.findByNotionPageId("page-1")).willReturn(Optional.of(existing));
        given(roadmapRepository.findAllByNotionPageIdIsNotNull()).willReturn(List.of(existing));

        RoadmapSyncResult result = syncService.syncFromNotion();

        assertThat(existing.getTitle()).isEqualTo("SQLD 취득");
        assertThat(existing.getStatus()).isEqualTo(RoadmapStatus.SUCCEEDED);
        assertThat(existing.getSortOrder()).isEqualTo(2);
        assertThat(existing.getNotionLastEditedAt()).isEqualTo(newTime);
        verify(roadmapRepository, never()).save(any());
        assertThat(result).isEqualTo(new RoadmapSyncResult(0, 1, 0, 0));
    }

    @Test
    void 노션_수정시각이_같거나_이전이면_skip된다() {
        OffsetDateTime savedTime = OffsetDateTime.now();
        Roadmap existing = saved("page-1", "sqld-pass", savedTime);
        given(notionClient.queryRoadmapDatabase()).willReturn(List.of(page("page-1", "SQLD 취득", "sqld-pass", "succeeded", savedTime, 1)));
        given(roadmapRepository.findByNotionPageId("page-1")).willReturn(Optional.of(existing));
        given(roadmapRepository.findAllByNotionPageIdIsNotNull()).willReturn(List.of(existing));

        RoadmapSyncResult result = syncService.syncFromNotion();

        verify(roadmapRepository, never()).save(any());
        assertThat(result).isEqualTo(new RoadmapSyncResult(0, 0, 0, 0));
    }

    @Test
    void 필수_필드가_없으면_skip되고_삭제_대상도_아니다() {
        OffsetDateTime now = OffsetDateTime.now();
        Roadmap existing = saved("page-1", "sqld-pass", now);
        given(notionClient.queryRoadmapDatabase()).willReturn(List.of(page("page-1", "제목", null, "succeeded", now, 1)));
        given(roadmapRepository.findAllByNotionPageIdIsNotNull()).willReturn(List.of(existing));

        RoadmapSyncResult result = syncService.syncFromNotion();

        verify(roadmapRepository, never()).save(any());
        verify(roadmapRepository, never()).delete(any());
        assertThat(result).isEqualTo(new RoadmapSyncResult(0, 0, 1, 0));
    }

    @Test
    void 노션에서_지운_항목은_DB에서도_삭제된다() {
        OffsetDateTime now = OffsetDateTime.now();
        Roadmap kept = saved("page-1", "uxn-join", now);
        Roadmap removed = saved("page-2", "skku-master", now);
        given(notionClient.queryRoadmapDatabase()).willReturn(List.of(page("page-1", "UXN 합류", "uxn-join", "in_progress", now, 1)));
        given(roadmapRepository.findByNotionPageId("page-1")).willReturn(Optional.of(kept));
        given(roadmapRepository.findAllByNotionPageIdIsNotNull()).willReturn(List.of(kept, removed));

        RoadmapSyncResult result = syncService.syncFromNotion();

        verify(roadmapRepository).delete(removed);
        verify(roadmapRepository, never()).delete(kept);
        assertThat(result).isEqualTo(new RoadmapSyncResult(0, 0, 0, 1));
    }

    @Test
    void 노션이_0건이면_안전장치로_아무것도_삭제하지_않는다() {
        given(notionClient.queryRoadmapDatabase()).willReturn(List.of());

        RoadmapSyncResult result = syncService.syncFromNotion();

        verify(roadmapRepository, never()).findAllByNotionPageIdIsNotNull();
        verify(roadmapRepository, never()).delete(any());
        assertThat(result).isEqualTo(new RoadmapSyncResult(0, 0, 0, 0));
    }
}
