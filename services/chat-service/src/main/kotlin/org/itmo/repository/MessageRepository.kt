package org.itmo.repository

import org.itmo.db.PostgresClient
import org.itmo.model.Message

class MessageRepository(private val postgresClient: PostgresClient) {

    fun createMessage(text: String, chatId: Long, userId: Long): Int {
        val query = """
            INSERT INTO messages (content, sender_id, chat_id)
            VALUES ('${text}', '${userId}', '${chatId}')
            ON CONFLICT DO NOTHING
        """.trimIndent()
        return postgresClient.executeUpdate(query)
    }

    fun getMessagesByChatId(chatId: Long): List<Message> {
        val query = """
            SELECT id, content, sender_id, created_at, chat_id
            FROM messages
            WHERE chat_id = '$chatId'
            ORDER BY created_at DESC
        """.trimIndent()

        return postgresClient.executeQuery(query) { rs ->
            Message(
                id = rs.getLong("id"),
                content = rs.getString("content"),
                senderId = rs.getLong("sender_id"),
                createdAt = rs.getTimestamp("created_at").toInstant(),
                chatId = rs.getLong("chat_id"),
            )
        }
    }

    fun getMessagesByIds(ids: List<Long>): List<Message> {
        val query = """
            SELECT id, content, sender_id, created_at, chat_id
            FROM messages
            WHERE id in (${ids.joinToString(",")})
            ORDER BY created_at DESC
        """.trimIndent()

        return postgresClient.executeQuery(query) { rs ->
            Message(
                id = rs.getLong("id"),
                content = rs.getString("content"),
                senderId = rs.getLong("sender_id"),
                createdAt = rs.getTimestamp("created_at").toInstant(),
                chatId = rs.getLong("chat_id"),
            )
        }
    }

    fun getMessagesByChatInterval(chatId: Long, interval: Long): List<Message> {
        val query = """
            SELECT id, content, sender_id, created_at, chat_id
            FROM messages
            WHERE chat_id = $chatId AND created_at >= now() - INTERVAL '$interval minutes'
            ORDER BY created_at DESC
        """.trimIndent()

        return postgresClient.executeQuery(query) { rs ->
            Message(
                id = rs.getLong("id"),
                content = rs.getString("content"),
                senderId = rs.getLong("sender_id"),
                createdAt = rs.getTimestamp("created_at").toInstant(),
                chatId = rs.getLong("chat_id"),
            )
        }
    }

    fun deleteMessage(messageId: Long): Int {
        val query = "DELETE FROM messages WHERE id = '$messageId'"
        return postgresClient.executeUpdate(query)
    }
}