CREATE TABLE IF NOT EXISTS user_info
(
    id             BIGSERIAL PRIMARY KEY,
    public_id      UUID         NOT NULL, -- Generated in code
    provider       VARCHAR(255) NOT NULL,
    sub            VARCHAR(255) NOT NULL,
    email          VARCHAR(255) NOT NULL,
    given_name     VARCHAR(255),
    family_name    VARCHAR(255),
    display_name   VARCHAR(255),
    password_hash  VARCHAR(255),
    email_verified BOOLEAN               DEFAULT FALSE,
    enabled        BOOLEAN               DEFAULT TRUE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    last_login     TIMESTAMPTZ  NULL,
    deleted_at     TIMESTAMPTZ  NULL,
    UNIQUE (public_id),
    UNIQUE (provider, sub),
    UNIQUE (provider, email)
);
CREATE INDEX IF NOT EXISTS idx_user_info__created_at ON user_info (created_at);
CREATE INDEX IF NOT EXISTS idx_user_info__last_login ON user_info (last_login);

CREATE TABLE IF NOT EXISTS user_roles
(
    user_id BIGINT       NOT NULL REFERENCES user_info (id),
    role    VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, role)
);
CREATE INDEX IF NOT EXISTS idx_user_roles__role ON user_roles (role);

CREATE TABLE IF NOT EXISTS user_verification
(
    id         BIGSERIAL PRIMARY KEY,
    token      UUID        NOT NULL,
    expiration TIMESTAMPTZ NOT NULL,
    user_id    BIGSERIAL REFERENCES user_info (id),
    UNIQUE (token)
);
CREATE INDEX IF NOT EXISTS idx_user_verification__user_id ON user_verification (user_id);
CREATE INDEX IF NOT EXISTS idx_user_verification__expiration ON user_verification (expiration);

-- =====================================================================
-- (Optional) password_reset_token
-- Separate from verification; included if you want distinct flows.
-- COMMENT OUT if not needed yet.
-- =====================================================================
-- CREATE TABLE password_reset_token (
--     id         BIGSERIAL PRIMARY KEY,
--     token      UUID        NOT NULL DEFAULT gen_random_uuid(),
--     user_id    BIGINT      NOT NULL REFERENCES user_info(id) ON DELETE CASCADE,
--     expiration TIMESTAMPTZ NOT NULL,
--     used_at    TIMESTAMPTZ,
--     UNIQUE (token)
-- );
-- CREATE INDEX IF NOT EXISTS idx_password_reset_token__user_id ON password_reset_token (user_id);
-- CREATE INDEX IF NOT EXISTS idx_password_reset_token__expiration ON password_reset_token (expiration);
