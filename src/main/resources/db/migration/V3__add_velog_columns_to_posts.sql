ALTER TABLE posts ADD COLUMN velog_post_id VARCHAR(64) UNIQUE;
ALTER TABLE posts ADD COLUMN velog_updated_at TIMESTAMPTZ;

CREATE INDEX idx_posts_velog_post_id ON posts (velog_post_id);
