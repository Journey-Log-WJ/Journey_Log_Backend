package com.wonjun.journeylog.api.post.dto;

import com.wonjun.journeylog.domain.tag.Tag;

public record TagResponse(Long id, String name) {

    public static TagResponse from(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName());
    }
}
