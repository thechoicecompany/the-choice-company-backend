-- ═══════════════════════════════════════════════════════════════════════════════
-- V4 — Product Images Table
-- Stores Cloudinary image metadata per product.
-- product_images: id, product_id, image_url, public_id, sort_order, is_primary, created_at
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS product_images (
    id          BIGSERIAL       PRIMARY KEY,
    product_id  BIGINT          NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url   VARCHAR(1000)   NOT NULL,
    public_id   VARCHAR(500)    NOT NULL,
    sort_order  INTEGER         NOT NULL DEFAULT 0,
    is_primary  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_sort_order_non_negative CHECK (sort_order >= 0)
);

-- Fast lookup of all images for a product, ordered for display
CREATE INDEX idx_product_images_product_id ON product_images (product_id);
CREATE INDEX idx_product_images_sort       ON product_images (product_id, sort_order ASC);

-- Partial unique index: only ONE primary image per product
CREATE UNIQUE INDEX idx_product_images_primary
    ON product_images (product_id)
    WHERE is_primary = TRUE;

-- ── Migrate existing single-image products into product_images ────────────────
-- Existing products have image URL in products.image column.
-- This seeds a primary ProductImage row for each existing product.
INSERT INTO product_images (product_id, image_url, public_id, sort_order, is_primary)
SELECT
    id,
    image,
    -- Extract a synthetic public_id from the URL path
    REGEXP_REPLACE(
        REGEXP_REPLACE(image, '^https?://[^/]+/[^/]+/[^/]+/upload/(v[0-9]+/)?', ''),
        '\.[a-zA-Z]+$', ''
    ),
    0,
    TRUE
FROM products
WHERE image IS NOT NULL AND image != ''
ON CONFLICT DO NOTHING;

-- ── Comments ──────────────────────────────────────────────────────────────────
COMMENT ON TABLE  product_images             IS 'Cloudinary image metadata per product';
COMMENT ON COLUMN product_images.image_url   IS 'Cloudinary secure_url — HTTPS';
COMMENT ON COLUMN product_images.public_id   IS 'Cloudinary public_id — used for deletion/transformation';
COMMENT ON COLUMN product_images.sort_order  IS '0-indexed display order; 0 = shown first';
COMMENT ON COLUMN product_images.is_primary  IS 'TRUE = shown in listing cards; enforced unique per product';
