INSERT INTO users (username, email, bio)
VALUES ('user1', 'email@mail.ru', 'empty');

INSERT INTO chats (name)
VALUES ('fisrt chat');

INSERT INTO messages (content, sender_id, chat_id)
VALUES ('сообщение 1', 1, 1),
       ('привет', 1, 1),
       ('сообщение 3', 1, 1);