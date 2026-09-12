ALTER TABLE products ADD COLUMN categories jsonb DEFAULT '[]'::jsonb;
ALTER TABLE products ADD COLUMN category_slugs jsonb DEFAULT '[]'::jsonb;
UPDATE products SET categories = jsonb_build_array(category),
                     category_slugs = jsonb_build_array(category_slug);
CREATE INDEX idx_products_category_slugs ON products USING GIN (category_slugs);