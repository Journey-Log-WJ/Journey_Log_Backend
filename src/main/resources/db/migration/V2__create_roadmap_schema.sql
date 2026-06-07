CREATE TABLE roadmaps (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    status VARCHAR(16) NOT NULL,
    target_date DATE,
    notion_page_id VARCHAR(64) UNIQUE,
    notion_last_edited_at TIMESTAMPTZ,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_roadmaps_status ON roadmaps (status);
CREATE INDEX idx_roadmaps_target_date ON roadmaps (target_date);
CREATE INDEX idx_roadmaps_notion_page_id ON roadmaps (notion_page_id);
