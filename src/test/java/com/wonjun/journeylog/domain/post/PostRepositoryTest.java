package com.wonjun.journeylog.domain.post;

import com.wonjun.journeylog.domain.tag.Tag;
import com.wonjun.journeylog.domain.tag.TagRepository;
import com.wonjun.journeylog.support.AbstractRepositoryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PostRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    @Test
    void save하면_createdAt과_updatedAt이_자동으로_채워진다() {
        Post post = Post.builder()
                .slug("hello-world")
                .title("Hello World")
                .content("My first post")
                .build();

        Post saved = postRepository.save(post);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findBySlug로_조회할_수_있다() {
        postRepository.save(Post.builder()
                .slug("find-by-slug")
                .title("T")
                .content("C")
                .build());

        Optional<Post> found = postRepository.findBySlug("find-by-slug");
        Optional<Post> notFound = postRepository.findBySlug("missing");

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("T");
        assertThat(notFound).isEmpty();
    }

    @Test
    void findByNotionPageId로_조회할_수_있다() {
        String notionId = "page-abcd-1234";
        postRepository.save(Post.builder()
                .slug("from-notion")
                .title("From Notion")
                .content("C")
                .notionPageId(notionId)
                .build());

        Optional<Post> found = postRepository.findByNotionPageId(notionId);

        assertThat(found).isPresent();
        assertThat(found.get().getSlug()).isEqualTo("from-notion");
    }

    @Test
    void publishedAt이_있는_post만_최신순으로_정렬되어_반환된다() {
        OffsetDateTime t1 = OffsetDateTime.now().minusDays(2);
        OffsetDateTime t2 = OffsetDateTime.now().minusDays(1);

        postRepository.save(Post.builder().slug("draft").title("Draft").content("c").build());
        postRepository.save(Post.builder().slug("p1").title("P1").content("c").publishedAt(t1).build());
        postRepository.save(Post.builder().slug("p2").title("P2").content("c").publishedAt(t2).build());

        List<Post> published = postRepository.findAllByPublishedAtIsNotNullOrderByPublishedAtDesc();

        assertThat(published).extracting(Post::getSlug).containsExactly("p2", "p1");
    }

    @Test
    void Post에_Tag를_연결하면_post_tags_조인이_저장된다() {
        Tag tag = tagRepository.save(Tag.builder().name("java").build());

        Post post = Post.builder().slug("with-tag").title("T").content("c").build();
        post.addTag(tag);
        postRepository.save(post);

        Post found = postRepository.findBySlug("with-tag").orElseThrow();

        assertThat(found.getTags())
                .extracting(Tag::getName)
                .containsExactly("java");
    }
}
