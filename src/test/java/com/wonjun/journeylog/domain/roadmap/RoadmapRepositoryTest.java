package com.wonjun.journeylog.domain.roadmap;

import com.wonjun.journeylog.support.AbstractRepositoryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RoadmapRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private RoadmapRepository roadmapRepository;

    @Test
    void save하면_createdAt과_updatedAt이_자동으로_채워진다() {
        Roadmap saved = roadmapRepository.save(Roadmap.builder()
                .slug("ship-mvp")
                .title("MVP 배포")
                .status(RoadmapStatus.PLANNING)
                .build());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getSortOrder()).isZero();
    }

    @Test
    void findBySlug로_조회할_수_있다() {
        roadmapRepository.save(Roadmap.builder()
                .slug("write-docs")
                .title("문서화")
                .status(RoadmapStatus.DOING)
                .build());

        Optional<Roadmap> found = roadmapRepository.findBySlug("write-docs");
        Optional<Roadmap> notFound = roadmapRepository.findBySlug("missing");

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(RoadmapStatus.DOING);
        assertThat(notFound).isEmpty();
    }

    @Test
    void findByNotionPageId로_조회할_수_있다() {
        roadmapRepository.save(Roadmap.builder()
                .slug("from-notion")
                .title("From Notion")
                .status(RoadmapStatus.PLANNING)
                .notionPageId("page-xyz")
                .build());

        Optional<Roadmap> found = roadmapRepository.findByNotionPageId("page-xyz");

        assertThat(found).isPresent();
        assertThat(found.get().getSlug()).isEqualTo("from-notion");
    }

    @Test
    void sortOrder_asc_targetDate_asc_순으로_정렬된다() {
        roadmapRepository.save(Roadmap.builder()
                .slug("a").title("A").status(RoadmapStatus.PLANNING)
                .sortOrder(2).targetDate(LocalDate.of(2026, 7, 1)).build());
        roadmapRepository.save(Roadmap.builder()
                .slug("b").title("B").status(RoadmapStatus.DOING)
                .sortOrder(1).targetDate(LocalDate.of(2026, 8, 1)).build());
        roadmapRepository.save(Roadmap.builder()
                .slug("c").title("C").status(RoadmapStatus.PLANNING)
                .sortOrder(1).targetDate(LocalDate.of(2026, 7, 1)).build());

        List<Roadmap> all = roadmapRepository.findAllByOrderBySortOrderAscTargetDateAsc();

        assertThat(all).extracting(Roadmap::getSlug).containsExactly("c", "b", "a");
    }
}
