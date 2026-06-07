package com.wonjun.journeylog.service.post;

import com.wonjun.journeylog.api.post.dto.PostDetailResponse;
import com.wonjun.journeylog.api.post.dto.PostListItemResponse;
import com.wonjun.journeylog.domain.post.Post;
import com.wonjun.journeylog.domain.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    public List<PostListItemResponse> findAllPublished() {
        return postRepository.findAllByPublishedAtIsNotNullOrderByPublishedAtDesc().stream()
                .map(PostListItemResponse::from)
                .toList();
    }

    public PostDetailResponse findBySlug(String slug) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new PostNotFoundException(slug));
        return PostDetailResponse.from(post);
    }
}
