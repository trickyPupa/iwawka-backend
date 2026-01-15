INSERT INTO chats (name)
VALUES ('fisrt chat');

INSERT INTO chat_user (user_id, chat_id)
VALUES (1, 1);

INSERT INTO messages (content, sender_id, chat_id)
VALUES ('сообщение 1', 1, 1),
       ('привет', 1, 1),
       ('сообщение 3', 1, 1);