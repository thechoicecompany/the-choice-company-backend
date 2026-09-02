-- ═══════════════════════════════════════════════════════════════
-- V3 — Admin Feature: Product Inventory + Catalogue Requests
-- Sprint: Admin Panel v3
-- ═══════════════════════════════════════════════════════════════

-- ── PRODUCT INVENTORY ─────────────────────────────────────────
-- One-to-one with products table.
-- Tracks stock levels, reorder alerts, and warehouse info.
CREATE TABLE product_inventory (
    id                  BIGSERIAL PRIMARY KEY,
    product_id          BIGINT        NOT NULL UNIQUE REFERENCES products(id) ON DELETE CASCADE,
    stock_qty           INTEGER       NOT NULL DEFAULT 0,
    reserved_qty        INTEGER       NOT NULL DEFAULT 0,
    reorder_level       INTEGER       NOT NULL DEFAULT 50,
    max_stock_qty       INTEGER                DEFAULT 10000,
    last_restocked_at   TIMESTAMPTZ,
    sku                 VARCHAR(100),
    warehouse_notes     VARCHAR(500),
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    -- Constraint: reserved cannot exceed stock
    CONSTRAINT chk_reserved_lte_stock CHECK (reserved_qty <= stock_qty),
    -- Constraint: stock cannot be negative
    CONSTRAINT chk_stock_non_negative  CHECK (stock_qty >= 0),
    -- Constraint: reorder level must be positive
    CONSTRAINT chk_reorder_positive    CHECK (reorder_level >= 0)
);

CREATE INDEX idx_inventory_product_id   ON product_inventory(product_id);
CREATE INDEX idx_inventory_low_stock    ON product_inventory((stock_qty - reserved_qty))
    WHERE (stock_qty - reserved_qty) <= reorder_level;

-- Auto-update trigger for product_inventory
CREATE TRIGGER trg_inventory_updated_at
    BEFORE UPDATE ON product_inventory
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

-- ── CATALOGUE REQUESTS ────────────────────────────────────────
-- Tracks every catalogue download request on the website.
-- Source: exit_popup | contact_page | blog | footer | direct
CREATE TABLE catalogue_requests (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    company_name    VARCHAR(100),
    phone           VARCHAR(15),
    source          VARCHAR(50)  NOT NULL DEFAULT 'exit_popup',
    page_url        VARCHAR(500),
    ip_address      VARCHAR(50),
    email_sent      BOOLEAN      DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_catalogue_email      ON catalogue_requests(email);
CREATE INDEX idx_catalogue_created_at ON catalogue_requests(created_at DESC);
CREATE INDEX idx_catalogue_source     ON catalogue_requests(source);

-- ── SEED INITIAL INVENTORY FOR EXISTING PRODUCTS ──────────────
-- Auto-create a 0-stock inventory record for every existing product
-- so no product is missing from the inventory table.
INSERT INTO product_inventory (product_id, stock_qty, reserved_qty, reorder_level)
SELECT id, 0, 0, 50
FROM products
WHERE id NOT IN (SELECT product_id FROM product_inventory);

