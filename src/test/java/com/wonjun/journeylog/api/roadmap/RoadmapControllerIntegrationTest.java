package com.wonjun.journeylog.api.roadmap;

import com.wonjun.journeylog.domain.roadmap.Roadmap;
import com.wonjun.journeylog.domain.roadmap.RoadmapRepository;
import com.wonjun.journeylog.domain.roadmap.RoadmapStatus;
import com.wonjun.journeylog.support.AbstractApiIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RoadmapControllerIntegrationTest extends AbstractApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoadmapRepository roadmapRepository;

    @BeforeEach
    void cleanUp() {
        roadmapRepository.deleteAll();
    }

    @Test
    void GET_roadmaps는_sortOrder_targetDate_순으로_반환한다() throws Exception {
        roadmapRepository.save(Roadmap.builder()
                .slug("later").title("Later").status(RoadmapStatus.PLANNING)
                .sortOrder(2).targetDate(LocalDate.of(2026, 9, 1)).build());
        roadmapRepository.save(Roadmap.builder()
                .slug("now").title("Now doing").status(RoadmapStatus.DOING)
                .sortOrder(1).targetDate(LocalDate.of(2026, 7, 1)).build());
        roadmapRepository.save(Roadmap.builder()
                .slug("soon").title("Soon").status(RoadmapStatus.PLANNING)
                .sortOrder(1).targetDate(LocalDate.of(2026, 8, 1)).build());

        mockMvc.perform(get("/api/roadmaps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].slug").value("now"))
                .andExpect(jsonPath("$[0].status").value("DOING"))
                .andExpect(jsonPath("$[1].slug").value("soon"))
                .andExpect(jsonPath("$[2].slug").value("later"));
    }

    @Test
    void GET_roadmaps는_비어있으면_빈배열을_반환한다() throws Exception {
        mockMvc.perform(get("/api/roadmaps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
