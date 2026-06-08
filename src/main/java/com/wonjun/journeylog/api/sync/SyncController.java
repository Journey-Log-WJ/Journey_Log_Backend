package com.wonjun.journeylog.api.sync;

import com.wonjun.journeylog.service.sync.PostSyncResult;
import com.wonjun.journeylog.service.sync.PostSyncService;
import com.wonjun.journeylog.service.sync.RoadmapSyncResult;
import com.wonjun.journeylog.service.sync.RoadmapSyncService;
import com.wonjun.journeylog.service.sync.SeriesSyncResult;
import com.wonjun.journeylog.service.sync.SeriesSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/sync")
@RequiredArgsConstructor
public class SyncController {

    private final RoadmapSyncService roadmapSyncService;
    private final PostSyncService postSyncService;
    private final SeriesSyncService seriesSyncService;

    @PostMapping("/roadmaps")
    public RoadmapSyncResult syncRoadmaps() {
        return roadmapSyncService.syncFromNotion();
    }

    @PostMapping("/posts")
    public PostSyncResult syncPosts() {
        return postSyncService.syncFromVelog();
    }

    @PostMapping("/series")
    public SeriesSyncResult syncSeries() {
        return seriesSyncService.syncFromVelog();
    }
}
