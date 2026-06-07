package com.wonjun.journeylog.domain.post;

import com.wonjun.journeylog.domain.tag.Tag;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 1000)
    private String excerpt;

    @Column(name = "notion_page_id", unique = true, length = 64)
    private String notionPageId;

    @Column(name = "notion_last_edited_at")
    private OffsetDateTime notionLastEditedAt;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @ManyToMany
    @JoinTable(
        name = "post_tags",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Builder
    private Post(String slug, String title, String content, String excerpt,
                 String notionPageId, OffsetDateTime notionLastEditedAt,
                 OffsetDateTime publishedAt) {
        this.slug = slug;
        this.title = title;
        this.content = content;
        this.excerpt = excerpt;
        this.notionPageId = notionPageId;
        this.notionLastEditedAt = notionLastEditedAt;
        this.publishedAt = publishedAt;
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

    public void updateContent(String title, String content, String excerpt,
                              OffsetDateTime notionLastEditedAt) {
        this.title = title;
        this.content = content;
        this.excerpt = excerpt;
        this.notionLastEditedAt = notionLastEditedAt;
    }

    public void publish(OffsetDateTime at) {
        this.publishedAt = at;
    }

    public void unpublish() {
        this.publishedAt = null;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public void clearTags() {
        this.tags.clear();
    }
}
