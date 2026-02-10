CREATE TABLE IF NOT EXISTS room (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL,
    name VARCHAR(80) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_room_branch FOREIGN KEY (branch_id) REFERENCES branch (id)
);

CREATE INDEX IF NOT EXISTS idx_room_branch_active ON room (branch_id, is_active);
CREATE UNIQUE INDEX IF NOT EXISTS uq_room_branch_name ON room (branch_id, name);

CREATE TABLE IF NOT EXISTS service (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL,
    name VARCHAR(120) NOT NULL,
    duration_minutes INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_service_branch FOREIGN KEY (branch_id) REFERENCES branch (id)
);

CREATE INDEX IF NOT EXISTS idx_service_branch_active ON service (branch_id, is_active);

INSERT INTO service (id, branch_id, name, duration_minutes, is_active, created_at)
SELECT
    '18aa6e96-9d53-4ad2-9d1a-f3f18b1258b1'::uuid,
    b.id,
    'Consulta general',
    30,
    TRUE,
    NOW()
FROM branch b
WHERE b.code = 'CENTRO'
  AND NOT EXISTS (
      SELECT 1
      FROM service s
      WHERE s.branch_id = b.id
        AND s.name = 'Consulta general'
  );

INSERT INTO service (id, branch_id, name, duration_minutes, is_active, created_at)
SELECT
    '79ea3457-d84a-40be-99c9-4f4d61adcb64'::uuid,
    b.id,
    'Vacunacion',
    45,
    TRUE,
    NOW()
FROM branch b
WHERE b.code = 'CENTRO'
  AND NOT EXISTS (
      SELECT 1
      FROM service s
      WHERE s.branch_id = b.id
        AND s.name = 'Vacunacion'
  );

CREATE TABLE IF NOT EXISTS room_block (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL,
    room_id UUID NOT NULL,
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    reason VARCHAR(255) NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_room_block_branch FOREIGN KEY (branch_id) REFERENCES branch (id),
    CONSTRAINT fk_room_block_room FOREIGN KEY (room_id) REFERENCES room (id),
    CONSTRAINT fk_room_block_created_by FOREIGN KEY (created_by) REFERENCES app_user (id)
);

CREATE INDEX IF NOT EXISTS idx_room_block_branch_room_starts ON room_block (branch_id, room_id, starts_at);

ALTER TABLE appointment
    ADD COLUMN IF NOT EXISTS service_id UUID NULL,
    ADD COLUMN IF NOT EXISTS checked_in_at TIMESTAMPTZ NULL,
    ADD COLUMN IF NOT EXISTS is_overbook BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS overbook_reason VARCHAR(255) NULL;

DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_appointment_room'
    ) THEN
        ALTER TABLE appointment
            ADD CONSTRAINT fk_appointment_room FOREIGN KEY (room_id) REFERENCES room (id);
    END IF;
END
$$;

DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_appointment_service'
    ) THEN
        ALTER TABLE appointment
            ADD CONSTRAINT fk_appointment_service FOREIGN KEY (service_id) REFERENCES service (id);
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_appointment_branch_room_starts ON appointment (branch_id, room_id, starts_at);
CREATE INDEX IF NOT EXISTS idx_appointment_branch_status_starts ON appointment (branch_id, status, starts_at);

CREATE TABLE IF NOT EXISTS audit_event (
    id UUID PRIMARY KEY,
    branch_id UUID NULL,
    actor_id UUID NOT NULL,
    action_code VARCHAR(80) NOT NULL,
    entity_name VARCHAR(80) NOT NULL,
    entity_id UUID NOT NULL,
    reason VARCHAR(255) NULL,
    before_json TEXT NULL,
    after_json TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_audit_event_branch FOREIGN KEY (branch_id) REFERENCES branch (id),
    CONSTRAINT fk_audit_event_actor FOREIGN KEY (actor_id) REFERENCES app_user (id)
);

CREATE INDEX IF NOT EXISTS idx_audit_event_branch_created ON audit_event (branch_id, created_at DESC);
