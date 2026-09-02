-- ═══════════════════════════════════════════════════════════════
-- V2 — Seed default Super Admin user
-- Password: Admin@TCC2026 (BCrypt strength 10)
-- CHANGE PASSWORD IMMEDIATELY AFTER FIRST LOGIN
-- ═══════════════════════════════════════════════════════════════
INSERT INTO users (email, password, full_name, role, is_active)
VALUES (
    'admin@thechoicecompany.in',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lih2',
    'Super Admin',
    'SUPER_ADMIN',
    TRUE
) ON CONFLICT (email) DO NOTHING;
