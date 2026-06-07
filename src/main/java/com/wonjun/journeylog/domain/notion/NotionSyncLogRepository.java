package com.wonjun.journeylog.domain.notion;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotionSyncLogRepository extends JpaRepository<NotionSyncLog, Long> {
}
