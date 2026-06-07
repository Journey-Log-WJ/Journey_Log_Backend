package com.wonjun.journeylog.api.sync;

import com.wonjun.journeylog.service.sync.RoadmapSyncResult;
import com.wonjun.journeylog.service.sync.RoadmapSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/sync")
@RequiredArgsConstructor
public class SyncController {

    private final RoadmapSyncService roadmapSyncService;

    @PostMapping("/roadmaps")
    public RoadmapSyncResult syncRoadmaps() {
        return roadmapSyncService.syncFromNotion();
    }
}
