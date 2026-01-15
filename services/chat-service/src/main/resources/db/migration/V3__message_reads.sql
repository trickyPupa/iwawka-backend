CREATE TABLE IF NOT EXISTS message_reads (
    id SERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    read_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (message_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_message_reads_user
ON message_reads(user_id);

CREATE INDEX IF NOT EXISTS idx_message_reads_message
ON message_reads(message_id);