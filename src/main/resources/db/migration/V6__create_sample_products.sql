-- Adjust the Vxx__ prefix number to match your Flyway sequence.

CREATE TABLE sample_products (
    id                BIGSERIAL PRIMARY KEY,
    name              VARCHAR(200)  NOT NULL,
    slug              VARCHAR(220)  NOT NULL UNIQUE,
    category          VARCHAR(100)  NOT NULL,
    category_slug     VARCHAR(100),
    description       VARCHAR(500)  NOT NULL,
    image             VARCHAR(500)  NOT NULL,
    images            JSONB         DEFAULT '[]',
    sample_price      NUMERIC(10,2) NOT NULL,
    bulk_price        NUMERIC(10,2) NOT NULL,
    max_sample_qty    INTEGER       NOT NULL DEFAULT 5,
    moq               INTEGER       NOT NULL,
    shipping_days     INTEGER       NOT NULL DEFAULT 5,
    material          VARCHAR(255),
    dimensions        VARCHAR(255),
    weight            VARCHAR(255),
    branding_options  JSONB         DEFAULT '[]',
    tags              JSONB         DEFAULT '[]',
    is_active         BOOLEAN       NOT NULL DEFAULT TRUE,
    is_bestseller     BOOLEAN       NOT NULL DEFAULT FALSE,
    sort_order        INTEGER       DEFAULT 0,
    meta_title        VARCHAR(200),
    meta_description  VARCHAR(500),
    created_at        TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sample_products_category   ON sample_products (category);
CREATE INDEX idx_sample_products_is_active  ON sample_products (is_active);

CREATE TABLE sample_product_images (
    id                 BIGSERIAL PRIMARY KEY,
    sample_product_id  BIGINT NOT NULL REFERENCES sample_products(id) ON DELETE CASCADE,
    image_url          VARCHAR(500) NOT NULL,
    public_id          VARCHAR(200),
    sort_order         INTEGER NOT NULL DEFAULT 0,
    is_primary         BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_sample_product_images_product ON sample_product_images (sample_product_id);