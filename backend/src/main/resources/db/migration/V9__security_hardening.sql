ALTER TABLE app_user
    ADD COLUMN IF NOT EXISTS totp_secret VARCHAR(512) NULL,
    ADD COLUMN IF NOT EXISTS totp_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS totp_verified_at TIMESTAMPTZ NULL;

CREATE TABLE IF NOT EXISTS auth_login_attempt (
    id UUID PRIMARY KEY,
    user_id UUID NULL,
    username VARCHAR(160) NOT NULL,
    ip VARCHAR(64) NULL,
    successful BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_auth_login_attempt_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

CREATE INDEX IF NOT EXISTS idx_auth_login_attempt_user_created
    ON auth_login_attempt (user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_auth_login_attempt_username_created
    ON auth_login_attempt (username, created_at DESC);

CREATE TABLE IF NOT EXISTS auth_2fa_challenge (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    branch_id UUID NULL,
    challenge_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_auth_2fa_challenge_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_auth_2fa_challenge_branch FOREIGN KEY (branch_id) REFERENCES branch (id)
);

CREATE INDEX IF NOT EXISTS idx_auth_2fa_challenge_expires
    ON auth_2fa_challenge (expires_at);
