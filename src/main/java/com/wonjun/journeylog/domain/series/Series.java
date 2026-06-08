package com.wonjun.journeylog.domain.series;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.OffsetDateTime;

@Entity
@Table(name = "series")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Series {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "velog_series_id", nullable = false, unique = true, length = 64)
    private String velogSeriesId;

    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "posts_count", nullable = false)
    private int postsCount;

    @Column(name = "velog_updated_at")
    private OffsetDateTime velogUpdatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Builder
    private Series(String velogSeriesId, String slug, String name, String description,
                   int postsCount, OffsetDateTime velogUpdatedAt) {
        this.velogSeriesId = velogSeriesId;
        this.slug = slug;
        this.name = name;
        this.description = description;
        this.postsCount = postsCount;
        this.velogUpdatedAt = velogUpdatedAt;
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

    public void updateFromVelog(String name, String description, int postsCount,
                                OffsetDateTime velogUpdatedAt) {
        this.name = name;
        this.description = description;
        this.postsCount = postsCount;
        this.velogUpdatedAt = velogUpdatedAt;
    }
}
