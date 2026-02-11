ALTER TABLE audit_event
    ADD COLUMN IF NOT EXISTS actor_username VARCHAR(120) NULL,
    ADD COLUMN IF NOT EXISTS is_sensitive BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS ip VARCHAR(64) NULL,
    ADD COLUMN IF NOT EXISTS user_agent VARCHAR(255) NULL;

CREATE INDEX IF NOT EXISTS idx_audit_event_created
    ON audit_event (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_audit_event_action_created
    ON audit_event (action_code, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_audit_event_entity
    ON audit_event (entity_name, entity_id);
