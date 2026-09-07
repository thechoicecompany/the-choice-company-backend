-- V5__gallery_file_fields.sql
ALTER TABLE gallery_items
  ADD COLUMN file_type     VARCHAR(20)  NOT NULL DEFAULT 'image',
  ADD COLUMN file_key      VARCHAR(500) NOT NULL DEFAULT '',
  ADD COLUMN thumbnail_key VARCHAR(500);