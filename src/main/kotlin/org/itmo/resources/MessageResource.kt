package org.itmo.resources

import org.itmo.db.PostgresClient
import org.itmo.model.Message
import java.time.LocalDateTime

class MessageResource(private val postgresClient: PostgresClient) {

    fun createMessage(message: Message): Int {
        val query = """
            INSERT INTO messages (id, content, sender_id, timestamp)
            VALUES ('${message.id}', '${message.content}', '${message.senderId}')
            ON CONFLICT (id) DO NOTHING
        """.trimIndent()
        return postgresClient.executeUpdate(query)
    }

    fun getMessagesByChatId(chatId: Long): List<Message> {
        val query = """
            SELECT id, content, sender_id, timestamp
            FROM messages
            WHERE chat_id = '$chatId'
            ORDER BY created DESC
        """.trimIndent()

        return postgresClient.executeQuery(query) { rs ->
            Message(
                id = rs.getLong("id"),
                content = rs.getString("content"),
                senderId = rs.getLong("sender_id"),
                created = LocalDateTime.parse(rs.getString("timestamp")),
                chatId = rs.getLong("chat_id"),
                deleted = LocalDateTime.parse(rs.getString("timestamp")),
            )
        }
    }

    fun deleteMessage(messageId: Long): Int {
        val query = "DELETE FROM messages WHERE id = '$messageId'"
        return postgresClient.executeUpdate(query)
    }
}