package com.wonjun.journeylog.service.post;

public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException(String slug) {
        super("Post not found: " + slug);
    }
}
