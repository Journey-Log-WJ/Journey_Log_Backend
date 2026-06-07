package com.wonjun.journeylog.domain.tag;

import com.wonjun.journeylog.support.AbstractRepositoryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TagRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @Test
    void save하면_createdAt이_자동으로_채워진다() {
        Tag saved = tagRepository.save(Tag.builder().name("spring").build());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void findByName으로_조회할_수_있다() {
        tagRepository.save(Tag.builder().name("kotlin").build());

        Optional<Tag> found = tagRepository.findByName("kotlin");
        Optional<Tag> notFound = tagRepository.findByName("missing");

        assertThat(found).isPresent();
        assertThat(notFound).isEmpty();
    }
}
