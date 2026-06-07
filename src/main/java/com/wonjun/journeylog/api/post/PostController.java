package com.wonjun.journeylog.api.post;

import com.wonjun.journeylog.api.post.dto.PostDetailResponse;
import com.wonjun.journeylog.api.post.dto.PostListItemResponse;
import com.wonjun.journeylog.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public List<PostListItemResponse> list() {
        return postService.findAllPublished();
    }

    @GetMapping("/{slug}")
    public PostDetailResponse detail(@PathVariable String slug) {
        return postService.findBySlug(slug);
    }
}
