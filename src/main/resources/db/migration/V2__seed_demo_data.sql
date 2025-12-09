INSERT INTO users (username, email)
VALUES
    ('alice', 'alice@example.com'),
    ('bob', 'bob@example.com'),
    ('charlie', 'charlie@example.com')
ON CONFLICT DO NOTHING;

INSERT INTO messages (content, sender_id, timestamp)
SELECT 'Hello from ' || username, id, extract(epoch FROM NOW())::BIGINT
FROM users
ON CONFLICT DO NOTHING;

