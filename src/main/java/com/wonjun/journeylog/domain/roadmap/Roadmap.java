package com.wonjun.journeylog.domain.roadmap;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "roadmaps")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Roadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String period;

    @Column(columnDefinition = "TEXT")
    private String story;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RoadmapStatus status;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "notion_page_id", unique = true, length = 64)
    private String notionPageId;

    @Column(name = "notion_last_edited_at")
    private OffsetDateTime notionLastEditedAt;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Builder
    private Roadmap(String slug, String title, String description, String period, String story,
                    RoadmapStatus status, LocalDate targetDate,
                    String notionPageId, OffsetDateTime notionLastEditedAt,
                    Integer sortOrder) {
        this.slug = slug;
        this.title = title;
        this.description = description;
        this.period = period;
        this.story = story;
        this.status = status;
        this.targetDate = targetDate;
        this.notionPageId = notionPageId;
        this.notionLastEditedAt = notionLastEditedAt;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public void update(String slug, String title, String description, String period, String story,
                       RoadmapStatus status, LocalDate targetDate,
                       Integer sortOrder, OffsetDateTime notionLastEditedAt) {
        this.slug = slug;
        this.title = title;
        this.description = description;
        this.period = period;
        this.story = story;
        this.status = status;
        this.targetDate = targetDate;
        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }
        this.notionLastEditedAt = notionLastEditedAt;
    }
}
