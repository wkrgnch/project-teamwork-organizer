ALTER TYPE request_status_type ADD VALUE IF NOT EXISTS 'NEEDS_CLARIFICATION';
ALTER TYPE request_status_type ADD VALUE IF NOT EXISTS 'CONVERTED_TO_TASK';

ALTER TABLE requests
    ADD COLUMN IF NOT EXISTS project_id INT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_requests_project'
    ) THEN
ALTER TABLE requests
    ADD CONSTRAINT fk_requests_project
        FOREIGN KEY (project_id)
            REFERENCES projects(id)
            ON DELETE CASCADE;
END IF;
END $$;
