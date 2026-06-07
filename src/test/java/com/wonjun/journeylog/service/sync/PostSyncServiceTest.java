package com.wonjun.journeylog.service.sync;

import com.wonjun.journeylog.domain.post.Post;
import com.wonjun.journeylog.domain.post.PostRepository;
import com.wonjun.journeylog.domain.tag.Tag;
import com.wonjun.journeylog.domain.tag.TagRepository;
import com.wonjun.journeylog.service.velog.VelogClient;
import com.wonjun.journeylog.service.velog.VelogPostDetail;
import com.wonjun.journeylog.service.velog.VelogPostSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostSyncServiceTest {

    @Mock
    private VelogClient velogClient;

    @Mock
    private PostRepository postRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private PostSyncService syncService;

    private VelogPostSummary summary(String id, String slug, OffsetDateTime updatedAt) {
        return new VelogPostSummary(
                id, "[Velog] 글 제목", slug,
                OffsetDateTime.now().minusDays(10), updatedAt,
                "short desc", List.of("Spring", "Java"));
    }

    private VelogPostDetail detail(String id, String slug, OffsetDateTime updatedAt, String body) {
        return new VelogPostDetail(
                id, "[Velog] 글 제목", slug,
                OffsetDateTime.now().minusDays(10), updatedAt,
                "short desc", body, List.of("Spring", "Java"));
    }

    @Test
    void 새_velog_글은_create로_저장된다() {
        OffsetDateTime now = OffsetDateTime.now();
        VelogPostSummary sum = summary("velog-1", "my-slug", now);
        VelogPostDetail det = detail("velog-1", "my-slug", now, "본문 markdown");

        given(velogClient.listPosts()).willReturn(List.of(sum));
        given(postRepository.findByVelogPostId("velog-1")).willReturn(Optional.empty());
        given(velogClient.readPost("my-slug")).willReturn(det);
        given(tagRepository.findByName(anyString())).willReturn(Optional.empty());
        given(tagRepository.save(any(Tag.class))).willAnswer(inv -> inv.getArgument(0));

        PostSyncResult result = syncService.syncFromVelog();

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post saved = captor.getValue();

        assertThat(saved.getSlug()).isEqualTo("my-slug");
        assertThat(saved.getContent()).isEqualTo("본문 markdown");
        assertThat(saved.getVelogPostId()).isEqualTo("velog-1");
        assertThat(saved.getVelogUpdatedAt()).isEqualTo(now);
        assertThat(saved.getTags()).extracting(Tag::getName).containsExactlyInAnyOrder("Spring", "Java");
        assertThat(result).isEqualTo(new PostSyncResult(1, 0, 0));
    }

    @Test
    void velog가_더_최신이면_기존_엔티티가_update된다() {
        OffsetDateTime oldTime = OffsetDateTime.now().minusHours(1);
        OffsetDateTime newTime = OffsetDateTime.now();

        Post existing = Post.builder()
                .slug("my-slug").title("(이전)").content("old body")
                .velogPostId("velog-1").velogUpdatedAt(oldTime)
                .build();

        VelogPostSummary sum = summary("velog-1", "my-slug", newTime);
        VelogPostDetail det = detail("velog-1", "my-slug", newTime, "새 본문");

        given(velogClient.listPosts()).willReturn(List.of(sum));
        given(postRepository.findByVelogPostId("velog-1")).willReturn(Optional.of(existing));
        given(velogClient.readPost("my-slug")).willReturn(det);
        given(tagRepository.findByName(anyString())).willReturn(Optional.empty());
        given(tagRepository.save(any(Tag.class))).willAnswer(inv -> inv.getArgument(0));

        PostSyncResult result = syncService.syncFromVelog();

        assertThat(existing.getContent()).isEqualTo("새 본문");
        assertThat(existing.getVelogUpdatedAt()).isEqualTo(newTime);
        verify(postRepository, never()).save(any());
        assertThat(result).isEqualTo(new PostSyncResult(0, 1, 0));
    }

    @Test
    void velog_updatedAt이_같거나_이전이면_skip된다() {
        OffsetDateTime savedTime = OffsetDateTime.now();

        Post existing = Post.builder()
                .slug("my-slug").title("기존").content("body")
                .velogPostId("velog-1").velogUpdatedAt(savedTime)
                .build();

        VelogPostSummary sum = summary("velog-1", "my-slug", savedTime);

        given(velogClient.listPosts()).willReturn(List.of(sum));
        given(postRepository.findByVelogPostId("velog-1")).willReturn(Optional.of(existing));

        PostSyncResult result = syncService.syncFromVelog();

        verify(velogClient, never()).readPost(anyString());
        verify(postRepository, never()).save(any());
        assertThat(result).isEqualTo(new PostSyncResult(0, 0, 0));
    }

    @Test
    void 필수_필드가_없으면_skipped_카운트가_증가한다() {
        VelogPostSummary invalid = new VelogPostSummary(
                "velog-1", null, "my-slug",
                OffsetDateTime.now(), OffsetDateTime.now(),
                "desc", List.of());

        given(velogClient.listPosts()).willReturn(List.of(invalid));

        PostSyncResult result = syncService.syncFromVelog();

        verify(postRepository, never()).save(any());
        assertThat(result).isEqualTo(new PostSyncResult(0, 0, 1));
    }
}
