CREATE TABLE posts (
    id                    BIGSERIAL PRIMARY KEY,
    slug                  VARCHAR(255) NOT NULL UNIQUE,
    title                 VARCHAR(500) NOT NULL,
    content               TEXT NOT NULL,
    excerpt               VARCHAR(1000),
    notion_page_id        VARCHAR(64) UNIQUE,
    notion_last_edited_at TIMESTAMPTZ,
    published_at          TIMESTAMPTZ,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_posts_published_at ON posts (published_at DESC);
CREATE INDEX idx_posts_notion_page_id ON posts (notion_page_id);

CREATE TABLE tags (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE post_tags (
    post_id BIGINT NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    tag_id  BIGINT NOT NULL REFERENCES tags (id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, tag_id)
);

CREATE INDEX idx_post_tags_tag_id ON post_tags (tag_id);

CREATE TABLE notion_sync_log (
    id             BIGSERIAL PRIMARY KEY,
    notion_page_id VARCHAR(64) NOT NULL,
    action         VARCHAR(32) NOT NULL,
    status         VARCHAR(16) NOT NULL,
    message        TEXT,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sync_log_page_id ON notion_sync_log (notion_page_id);
CREATE INDEX idx_sync_log_created_at ON notion_sync_log (created_at DESC);
