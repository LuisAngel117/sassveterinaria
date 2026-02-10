CREATE TABLE IF NOT EXISTS branch (
    id UUID PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS app_user (
    id UUID PRIMARY KEY,
    email VARCHAR(160) NOT NULL UNIQUE,
    full_name VARCHAR(160) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role_code VARCHAR(30) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    locked_until TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS user_branch (
    user_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (user_id, branch_id),
    CONSTRAINT fk_user_branch_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_user_branch_branch FOREIGN KEY (branch_id) REFERENCES branch (id)
);

CREATE TABLE IF NOT EXISTS auth_refresh_token (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    issued_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ NULL,
    replaced_by UUID NULL,
    CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

CREATE INDEX IF NOT EXISTS idx_auth_refresh_user_id ON auth_refresh_token (user_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_auth_refresh_token_hash ON auth_refresh_token (token_hash);

CREATE TABLE IF NOT EXISTS appointment (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL,
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(30) NOT NULL,
    reason VARCHAR(255) NULL,
    notes TEXT NULL,
    room_id UUID NULL,
    client_id UUID NULL,
    pet_id UUID NULL,
    veterinarian_id UUID NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_appointment_branch FOREIGN KEY (branch_id) REFERENCES branch (id)
);

CREATE INDEX IF NOT EXISTS idx_appointment_branch_starts_at ON appointment (branch_id, starts_at);
