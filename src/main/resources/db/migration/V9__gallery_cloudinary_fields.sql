-- V9__gallery_cloudinary_fields.sql

-- Add the new Cloudinary-backed columns
ALTER TABLE gallery_items
  ADD COLUMN thumbnail_url        VARCHAR(500),
  ADD COLUMN thumbnail_public_id  VARCHAR(300),
  ADD COLUMN file_url             VARCHAR(500),
  ADD COLUMN file_public_id       VARCHAR(300);

-- Backfill file_type default going forward — gallery items created via the
-- new admin flow are always PDFs for now
ALTER TABLE gallery_items
  ALTER COLUMN file_type SET DEFAULT 'pdf';

-- Drop the old local-disk columns from V5 — no longer read or written
-- by any code path (FileStorageService is being retired)
ALTER TABLE gallery_items
  DROP COLUMN file_key,
  DROP COLUMN thumbnail_key;

-- Drop the legacy unused "image" column from the original entity
ALTER TABLE gallery_items
  DROP COLUMN image;

-- Make the new required columns NOT NULL now that old data is gone/backfilled
-- Only run this if you have no existing rows you need to keep — see note below
ALTER TABLE gallery_items
  ALTER COLUMN thumbnail_url SET NOT NULL,
  ALTER COLUMN file_url      SET NOT NULL;