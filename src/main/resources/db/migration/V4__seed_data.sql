INSERT INTO users (username, email, bio, password_hash)
VALUES ('user1', 'email@mail.ru', 'empty', '123');

INSERT INTO chats (name)
VALUES ('fisrt chat');

INSERT INTO messages (content, sender_id, chat_id)
VALUES ('сообщение 1', 1, 1),
       ('привет', 1, 1),
       ('сообщение 3', 1, 1);