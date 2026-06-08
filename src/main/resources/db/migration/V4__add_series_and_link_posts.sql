CREATE TABLE series (
    id BIGSERIAL PRIMARY KEY,
    velog_series_id VARCHAR(64) NOT NULL UNIQUE,
    slug VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(500) NOT NULL,
    description TEXT,
    posts_count INT NOT NULL DEFAULT 0,
    velog_updated_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_series_velog_series_id ON series (velog_series_id);

ALTER TABLE posts ADD COLUMN series_id BIGINT REFERENCES series(id) ON DELETE SET NULL;
ALTER TABLE posts ADD COLUMN series_index INT;

CREATE INDEX idx_posts_series_id ON posts (series_id);
