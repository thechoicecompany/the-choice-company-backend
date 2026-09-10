-- V12__create_hero_banners.sql
-- V12__create_hero_banners.sql

CREATE TABLE hero_banners (
    id               BIGSERIAL PRIMARY KEY,

    tag              VARCHAR(120) NOT NULL,
    headline_top     VARCHAR(200) NOT NULL,
    headline_bottom  VARCHAR(200) NOT NULL,
    body             VARCHAR(400) NOT NULL,

    image_url        TEXT NOT NULL,
    image_public_id  TEXT NOT NULL,

    display_order    INTEGER NOT NULL DEFAULT 0,
    active           BOOLEAN NOT NULL DEFAULT TRUE,

    cta_link         VARCHAR(300),
    cta_label        VARCHAR(80),

    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_hero_banners_active_order
    ON hero_banners (active, display_order);