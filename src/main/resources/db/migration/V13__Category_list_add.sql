ALTER TABLE products ADD COLUMN IF NOT EXISTS categories jsonb DEFAULT '[]'::jsonb;
ALTER TABLE products ADD COLUMN IF NOT EXISTS category_slugs jsonb DEFAULT '[]'::jsonb;

UPDATE products 
SET categories = jsonb_build_array(category),
    category_slugs = jsonb_build_array(category_slug)
WHERE categories = '[]'::jsonb OR categories IS NULL;

CREATE INDEX IF NOT EXISTS idx_products_category_slugs ON products USING GIN (category_slugs);