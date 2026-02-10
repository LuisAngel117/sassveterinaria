CREATE TABLE IF NOT EXISTS client (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL,
    full_name VARCHAR(160) NOT NULL,
    identification VARCHAR(30) NULL,
    phone VARCHAR(30) NULL,
    email VARCHAR(160) NULL,
    address VARCHAR(255) NULL,
    notes TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_client_branch FOREIGN KEY (branch_id) REFERENCES branch (id)
);

ALTER TABLE client
    ADD COLUMN IF NOT EXISTS identification VARCHAR(30) NULL,
    ADD COLUMN IF NOT EXISTS notes TEXT NULL;

CREATE INDEX IF NOT EXISTS idx_client_branch_full_name ON client (branch_id, full_name);
CREATE INDEX IF NOT EXISTS idx_client_branch_phone ON client (branch_id, phone);
CREATE INDEX IF NOT EXISTS idx_client_branch_identification ON client (branch_id, identification);

CREATE TABLE IF NOT EXISTS pet (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL,
    client_id UUID NOT NULL,
    internal_code VARCHAR(30) NOT NULL,
    name VARCHAR(120) NOT NULL,
    species VARCHAR(80) NOT NULL,
    breed VARCHAR(120) NULL,
    sex VARCHAR(20) NULL,
    birth_date DATE NULL,
    weight_kg NUMERIC(6,2) NULL,
    neutered BOOLEAN NULL,
    alerts TEXT NULL,
    history TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_pet_branch FOREIGN KEY (branch_id) REFERENCES branch (id),
    CONSTRAINT fk_pet_client FOREIGN KEY (client_id) REFERENCES client (id)
);

ALTER TABLE pet
    ADD COLUMN IF NOT EXISTS internal_code VARCHAR(30) NULL,
    ADD COLUMN IF NOT EXISTS neutered BOOLEAN NULL,
    ADD COLUMN IF NOT EXISTS history TEXT NULL;

DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_pet_client'
    ) THEN
        ALTER TABLE pet
            ADD CONSTRAINT fk_pet_client FOREIGN KEY (client_id) REFERENCES client (id);
    END IF;
END
$$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_pet_branch_internal_code ON pet (branch_id, internal_code);
CREATE INDEX IF NOT EXISTS idx_pet_branch_client ON pet (branch_id, client_id);
