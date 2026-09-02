-- V13__create_product_images.sql
-- Product images table — stores Cloudinary image metadata per product.
-- The primary image (is_primary = true, sort_order = 0) mirrors the
-- products.image column for backward compatibility.

CREATE TABLE IF NOT EXISTS product_images (
    id          BIGSERIAL       PRIMARY KEY,
    product_id  BIGINT          NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url   VARCHAR(1000)   NOT NULL,
    public_id   VARCHAR(500)    NOT NULL DEFAULT '',
    sort_order  INT             NOT NULL DEFAULT 0,
    is_primary  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_sort_order_non_negative CHECK (sort_order >= 0)
);

-- One primary image per product enforced at DB level
CREATE UNIQUE INDEX IF NOT EXISTS uidx_product_images_primary
    ON product_images (product_id)
    WHERE is_primary = TRUE;

-- Fast lookup by product, ordered by display position
CREATE INDEX IF NOT EXISTS idx_product_images_product_sort
    ON product_images (product_id, sort_order ASC);