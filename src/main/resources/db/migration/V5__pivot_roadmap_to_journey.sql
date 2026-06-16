ALTER TABLE roadmaps ADD COLUMN period VARCHAR(100);
ALTER TABLE roadmaps ADD COLUMN story TEXT;

UPDATE roadmaps SET status = 'PLANNED' WHERE status = 'PLANNING';
UPDATE roadmaps SET status = 'IN_PROGRESS' WHERE status = 'DOING';
UPDATE roadmaps SET status = 'SUCCEEDED' WHERE status = 'DONE';
