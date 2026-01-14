CREATE TABLE IF NOT EXISTS images (
    id SERIAL PRIMARY KEY,
    data BYTEA NOT NULL,
    content_type VARCHAR(100) NOT NULL DEFAULT 'image/jpeg',
    size_bytes INTEGER NOT NULL,
    uploaded_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE users ADD COLUMN IF NOT EXISTS image_id BIGINT REFERENCES images(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_images_uploaded_by ON images(uploaded_by);
CREATE INDEX IF NOT EXISTS idx_users_image_id ON users(image_id);
