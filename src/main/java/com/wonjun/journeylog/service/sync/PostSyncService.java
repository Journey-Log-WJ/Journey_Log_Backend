package com.wonjun.journeylog.service.sync;

import com.wonjun.journeylog.domain.post.Post;
import com.wonjun.journeylog.domain.post.PostRepository;
import com.wonjun.journeylog.domain.tag.Tag;
import com.wonjun.journeylog.domain.tag.TagRepository;
import com.wonjun.journeylog.service.velog.VelogClient;
import com.wonjun.journeylog.service.velog.VelogPostDetail;
import com.wonjun.journeylog.service.velog.VelogPostSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostSyncService {

    private final VelogClient velogClient;
    private final PostRepository postRepository;
    private final TagRepository tagRepository;

    @Transactional
    public PostSyncResult syncFromVelog() {
        List<VelogPostSummary> summaries = velogClient.listPosts();

        int created = 0;
        int updated = 0;
        int skipped = 0;

        for (VelogPostSummary summary : summaries) {
            if (!isValid(summary)) {
                log.warn("Skipping invalid Velog summary (id={})", summary.id());
                skipped++;
                continue;
            }

            Optional<Post> existing = postRepository.findByVelogPostId(summary.id());

            if (existing.isEmpty()) {
                VelogPostDetail detail = velogClient.readPost(summary.urlSlug());
                if (detail == null) {
                    log.warn("readPost returned null for slug={}", summary.urlSlug());
                    skipped++;
                    continue;
                }
                Post post = Post.builder()
                        .slug(summary.urlSlug())
                        .title(detail.title())
                        .content(detail.body())
                        .excerpt(detail.shortDescription())
                        .publishedAt(detail.releasedAt())
                        .velogPostId(detail.id())
                        .velogUpdatedAt(detail.updatedAt())
                        .build();
                attachTags(post, detail.tags());
                postRepository.save(post);
                created++;
            } else {
                Post post = existing.get();
                if (isUnchanged(post, summary)) {
                    continue;
                }
                VelogPostDetail detail = velogClient.readPost(summary.urlSlug());
                if (detail == null) {
                    skipped++;
                    continue;
                }
                post.updateFromVelog(
                        detail.title(),
                        detail.body(),
                        detail.shortDescription(),
                        detail.releasedAt(),
                        detail.updatedAt()
                );
                post.clearTags();
                attachTags(post, detail.tags());
                updated++;
            }
        }

        log.info("Velog sync done — created={}, updated={}, skipped={}", created, updated, skipped);
        return new PostSyncResult(created, updated, skipped);
    }

    private boolean isValid(VelogPostSummary summary) {
        return summary.id() != null && !summary.id().isBlank()
                && summary.urlSlug() != null && !summary.urlSlug().isBlank()
                && summary.title() != null && !summary.title().isBlank();
    }

    private boolean isUnchanged(Post post, VelogPostSummary summary) {
        return post.getVelogUpdatedAt() != null
                && !summary.updatedAt().isAfter(post.getVelogUpdatedAt());
    }

    private void attachTags(Post post, List<String> tagNames) {
        for (String name : tagNames) {
            Tag tag = tagRepository.findByName(name)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(name).build()));
            post.addTag(tag);
        }
    }
}
