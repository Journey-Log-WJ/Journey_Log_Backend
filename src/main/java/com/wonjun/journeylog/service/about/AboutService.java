package com.wonjun.journeylog.service.about;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonjun.journeylog.domain.about.AboutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AboutService {

    private static final long ABOUT_ROW_ID = 1L;

    private final AboutRepository aboutRepository;
    private final ObjectMapper objectMapper;

    public Optional<JsonNode> get() {
        return aboutRepository.findById(ABOUT_ROW_ID)
                .map(a -> {
                    try {
                        return objectMapper.readTree(a.getContent());
                    } catch (Exception e) {
                        log.error("Failed to parse about content JSON", e);
                        return null;
                    }
                });
    }
}
