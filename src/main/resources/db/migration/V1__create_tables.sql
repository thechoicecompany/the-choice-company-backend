-- ═══════════════════════════════════════════════════════════════
-- The Choice Company — Flyway Migration V1
-- Creates all tables for Phase 1
-- ═══════════════════════════════════════════════════════════════

-- ── USERS ─────────────────────────────────────────────────────
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255) UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL,
    full_name   VARCHAR(100) NOT NULL,
    role        VARCHAR(30)  NOT NULL DEFAULT 'SALES_EXECUTIVE',
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    last_login  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ── INQUIRIES ─────────────────────────────────────────────────
CREATE TABLE inquiries (
    id                      BIGSERIAL PRIMARY KEY,
    ref_number              VARCHAR(20)  UNIQUE NOT NULL,
    company_name            VARCHAR(100) NOT NULL,
    contact_person          VARCHAR(100) NOT NULL,
    designation             VARCHAR(100),
    email                   VARCHAR(255) NOT NULL,
    mobile                  VARCHAR(15)  NOT NULL,
    city                    VARCHAR(60)  NOT NULL,
    state                   VARCHAR(60)  NOT NULL,
    product_category        VARCHAR(100) NOT NULL,
    quantity_required       INTEGER      NOT NULL,
    budget_range            VARCHAR(30)  NOT NULL,
    delivery_location       VARCHAR(200) NOT NULL,
    branding_required       BOOLEAN      DEFAULT FALSE,
    packaging_requirement   TEXT,
    expected_delivery_date  DATE,
    additional_notes        TEXT,
    logo_url                VARCHAR(500),
    source                  VARCHAR(50)  DEFAULT 'WEBSITE_FORM',
    status                  VARCHAR(30)  NOT NULL DEFAULT 'NEW',
    assigned_to             BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    whatsapp_sent           BOOLEAN      DEFAULT FALSE,
    email_sent              BOOLEAN      DEFAULT FALSE,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_inquiries_status      ON inquiries(status);
CREATE INDEX idx_inquiries_assigned_to ON inquiries(assigned_to);
CREATE INDEX idx_inquiries_created_at  ON inquiries(created_at DESC);
CREATE INDEX idx_inquiries_state       ON inquiries(state);

-- ── INQUIRY NOTES ─────────────────────────────────────────────
CREATE TABLE inquiry_notes (
    id          BIGSERIAL PRIMARY KEY,
    inquiry_id  BIGINT      NOT NULL REFERENCES inquiries(id) ON DELETE CASCADE,
    content     TEXT        NOT NULL,
    author_id   BIGINT      REFERENCES users(id) ON DELETE SET NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_inquiry_notes_inquiry_id ON inquiry_notes(inquiry_id);

-- ── PRODUCTS ──────────────────────────────────────────────────
CREATE TABLE products (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(200) NOT NULL,
    slug             VARCHAR(200) UNIQUE NOT NULL,
    category         VARCHAR(100) NOT NULL,
    category_slug    VARCHAR(100) NOT NULL,
    description      TEXT         NOT NULL,
    full_description TEXT,
    image            VARCHAR(500) NOT NULL,
    images           JSONB,
    moq              INTEGER      NOT NULL DEFAULT 50,
    base_price       DECIMAL(10,2) NOT NULL,
    material         VARCHAR(200),
    lead_time        VARCHAR(100),
    branding_options JSONB,
    specifications   JSONB,
    is_featured      BOOLEAN      DEFAULT FALSE,
    is_active        BOOLEAN      DEFAULT TRUE,
    sort_order       INTEGER      DEFAULT 0,
    tags             JSONB,
    meta_title       VARCHAR(200),
    meta_description VARCHAR(500),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_slug          ON products(slug);
CREATE INDEX idx_products_category_slug ON products(category_slug);
CREATE INDEX idx_products_is_featured   ON products(is_featured) WHERE is_active = TRUE;

-- ── PRODUCT PRICING TIERS ─────────────────────────────────────
CREATE TABLE product_pricing_tiers (
    id          BIGSERIAL PRIMARY KEY,
    product_id  BIGINT        NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    min_qty     INTEGER        NOT NULL,
    max_qty     INTEGER        NOT NULL,
    price       DECIMAL(10,2)  NOT NULL,
    label       VARCHAR(100),
    sort_order  INTEGER        DEFAULT 0
);

CREATE INDEX idx_pricing_tiers_product_id ON product_pricing_tiers(product_id);

-- ── BLOG POSTS ────────────────────────────────────────────────
CREATE TABLE blog_posts (
    id               BIGSERIAL PRIMARY KEY,
    slug             VARCHAR(200) UNIQUE NOT NULL,
    title            VARCHAR(300) NOT NULL,
    excerpt          VARCHAR(500) NOT NULL,
    content          TEXT         NOT NULL,
    featured_image   VARCHAR(500),
    category         VARCHAR(100) NOT NULL,
    tags             JSONB,
    author           VARCHAR(100) NOT NULL,
    read_time        INTEGER      DEFAULT 5,
    is_published     BOOLEAN      DEFAULT FALSE,
    meta_title       VARCHAR(200),
    meta_description VARCHAR(500),
    published_at     TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_blog_slug         ON blog_posts(slug);
CREATE INDEX idx_blog_published    ON blog_posts(is_published, published_at DESC);
CREATE INDEX idx_blog_category     ON blog_posts(category) WHERE is_published = TRUE;

-- ── GALLERY ITEMS ─────────────────────────────────────────────
CREATE TABLE gallery_items (
    id               BIGSERIAL PRIMARY KEY,
    image            VARCHAR(500) NOT NULL,
    caption          VARCHAR(300) NOT NULL,
    project_name     VARCHAR(200) NOT NULL,
    category         VARCHAR(50)  NOT NULL,
    client_industry  VARCHAR(100),
    quantity         INTEGER,
    sort_order       INTEGER      DEFAULT 0,
    is_active        BOOLEAN      DEFAULT TRUE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_gallery_category ON gallery_items(category) WHERE is_active = TRUE;

-- ── DEMO ORDERS ───────────────────────────────────────────────
CREATE TABLE demo_orders (
    id                   BIGSERIAL PRIMARY KEY,
    order_id             VARCHAR(30)   UNIQUE NOT NULL,
    razorpay_order_id    VARCHAR(100),
    razorpay_payment_id  VARCHAR(100),
    customer_name        VARCHAR(100)  NOT NULL,
    customer_email       VARCHAR(255)  NOT NULL,
    customer_phone       VARCHAR(15)   NOT NULL,
    shipping_address     JSONB         NOT NULL,
    items                JSONB         NOT NULL,
    subtotal             DECIMAL(10,2) NOT NULL,
    discount             DECIMAL(10,2) DEFAULT 0,
    coupon_code          VARCHAR(20),
    gst_amount           DECIMAL(10,2) NOT NULL,
    total                DECIMAL(10,2) NOT NULL,
    status               VARCHAR(30)   NOT NULL DEFAULT 'PAID',
    email_sent           BOOLEAN       DEFAULT FALSE,
    created_at           TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_order_id      ON demo_orders(order_id);
CREATE INDEX idx_orders_payment_id    ON demo_orders(razorpay_payment_id);
CREATE INDEX idx_orders_customer_email ON demo_orders(customer_email);

-- ── NEWSLETTER SUBSCRIBERS ────────────────────────────────────
CREATE TABLE newsletter_subscribers (
    id         BIGSERIAL PRIMARY KEY,
    email      VARCHAR(255) UNIQUE NOT NULL,
    is_active  BOOLEAN      DEFAULT TRUE,
    source     VARCHAR(50)  DEFAULT 'footer',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ── AUTO-UPDATE updated_at TRIGGER ───────────────────────────
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trg_inquiries_updated_at
    BEFORE UPDATE ON inquiries
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trg_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trg_blog_updated_at
    BEFORE UPDATE ON blog_posts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trg_orders_updated_at
    BEFORE UPDATE ON demo_orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();
