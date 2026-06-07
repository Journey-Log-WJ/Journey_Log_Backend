package com.wonjun.journeylog.api.post;

import com.wonjun.journeylog.domain.post.Post;
import com.wonjun.journeylog.domain.post.PostRepository;
import com.wonjun.journeylog.domain.tag.Tag;
import com.wonjun.journeylog.domain.tag.TagRepository;
import com.wonjun.journeylog.support.AbstractApiIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostControllerIntegrationTest extends AbstractApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    @BeforeEach
    void cleanUp() {
        postRepository.deleteAll();
        tagRepository.deleteAll();
    }

    @Test
    void GET_posts는_published된_post만_publishedAt_내림차순으로_반환한다() throws Exception {
        OffsetDateTime older = OffsetDateTime.now().minusDays(2);
        OffsetDateTime newer = OffsetDateTime.now().minusDays(1);

        postRepository.save(Post.builder().slug("draft").title("Draft").content("c").build());
        postRepository.save(Post.builder().slug("older").title("Older").content("c").publishedAt(older).build());
        postRepository.save(Post.builder().slug("newer").title("Newer").content("c").publishedAt(newer).build());

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].slug").value("newer"))
                .andExpect(jsonPath("$[1].slug").value("older"));
    }

    @Test
    void GET_posts_slug는_상세를_반환하고_tag도_포함한다() throws Exception {
        Tag java = tagRepository.save(Tag.builder().name("java").build());
        Tag spring = tagRepository.save(Tag.builder().name("spring").build());

        Post post = Post.builder()
                .slug("hello-world")
                .title("Hello World")
                .content("Body content")
                .excerpt("first post")
                .publishedAt(OffsetDateTime.now())
                .build();
        post.addTag(spring);
        post.addTag(java);
        postRepository.save(post);

        mockMvc.perform(get("/api/posts/{slug}", "hello-world"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("hello-world"))
                .andExpect(jsonPath("$.title").value("Hello World"))
                .andExpect(jsonPath("$.content").value("Body content"))
                .andExpect(jsonPath("$.excerpt").value("first post"))
                .andExpect(jsonPath("$.tags.length()").value(2))
                .andExpect(jsonPath("$.tags[0].name").value("java"))
                .andExpect(jsonPath("$.tags[1].name").value("spring"));
    }

    @Test
    void GET_posts_slug는_없으면_404와_ProblemDetail을_반환한다() throws Exception {
        mockMvc.perform(get("/api/posts/{slug}", "missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Post not found: missing"));
    }
}
