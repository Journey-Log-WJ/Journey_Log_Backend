package com.wonjun.journeylog.domain.notion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "notion_sync_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotionSyncLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notion_page_id", nullable = false, length = 64)
    private String notionPageId;

    @Column(nullable = false, length = 32)
    private String action;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Builder
    private NotionSyncLog(String notionPageId, String action, String status, String message) {
        this.notionPageId = notionPageId;
        this.action = action;
        this.status = status;
        this.message = message;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}
