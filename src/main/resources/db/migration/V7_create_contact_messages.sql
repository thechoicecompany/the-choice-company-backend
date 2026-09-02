-- ═══════════════════════════════════════════════════════════════
-- The Choice Company — Flyway Migration V7
-- Creates contact_messages table
-- ═══════════════════════════════════════════════════════════════

-- ── CONTACT MESSAGES ─────────────────────────────────────────

CREATE TABLE contact_messages (

    id           BIGSERIAL PRIMARY KEY,

    name         VARCHAR(255) NOT NULL,

    company      VARCHAR(255),

    email        VARCHAR(255) NOT NULL,

    phone        VARCHAR(255),

    subject      VARCHAR(255) NOT NULL,

    message      VARCHAR(2000) NOT NULL,

    status       VARCHAR(30) NOT NULL DEFAULT 'NEW',

    assigned_to  BIGINT REFERENCES users(id) ON DELETE SET NULL,

    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ── INDEXES ───────────────────────────────────────────────────

CREATE INDEX idx_contact_messages_status
    ON contact_messages(status);

CREATE INDEX idx_contact_messages_assigned_to
    ON contact_messages(assigned_to);

CREATE INDEX idx_contact_messages_created_at
    ON contact_messages(created_at DESC);

CREATE INDEX idx_contact_messages_email
    ON contact_messages(email);


-- ── AUTO-UPDATE updated_at ────────────────────────────────────

CREATE TRIGGER trg_contact_messages_updated_at

    BEFORE UPDATE ON contact_messages

    FOR EACH ROW EXECUTE FUNCTION update_updated_at();