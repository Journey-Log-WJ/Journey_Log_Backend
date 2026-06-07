package com.wonjun.journeylog.domain.notion;

import com.wonjun.journeylog.support.AbstractRepositoryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class NotionSyncLogRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private NotionSyncLogRepository repository;

    @Test
    void save하면_id와_createdAt이_자동으로_채워진다() {
        NotionSyncLog log = NotionSyncLog.builder()
                .notionPageId("page-1")
                .action("create")
                .status("success")
                .message("synced")
                .build();

        NotionSyncLog saved = repository.save(log);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getAction()).isEqualTo("create");
        assertThat(saved.getStatus()).isEqualTo("success");
    }
}
