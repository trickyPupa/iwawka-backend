package org.itmo.repository

import org.itmo.db.PostgresClient
import org.itmo.model.Chat
import org.slf4j.LoggerFactory
import kotlin.use

class ChatRepository(private val postgresClient: PostgresClient) {
    private val logger = LoggerFactory.getLogger(ChatRepository::class.java)

    /**
     * Создает новый чат в базе данных
     * @param name название чата
     * @return ID созданного чата
     */
    fun createChat(name: String): Long {
        val connection = postgresClient.getConnection()
        try {
            val sql = """
                INSERT INTO chats (name)
                VALUES (?)
                RETURNING id
            """.trimIndent()

            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, name)

                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    return resultSet.getLong("id")
                } else {
                    throw RuntimeException("Failed to create chat")
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to create chat: ${e.message}", e)
            throw RuntimeException("Failed to create chat: ${e.message}", e)
        } finally {
            connection.close()
        }
    }

    /**
     * Добавляет участников в чат
     * @param chatId чат
     * @param userId участник
     */
    fun addUserToChat(chatId: Long, userId: Long): Boolean {
        val connection = postgresClient.getConnection()
        try {
            val sql = """
                INSERT INTO chat_user (user_id, chat_id)
                VALUES (?, ?)
            """.trimIndent()

            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, userId)
                statement.setLong(2, chatId)

                val rowsAffected = statement.executeUpdate()
                return rowsAffected > 0
            }
        } finally {
            connection.close()
        }
    }

    fun getAllChats(): List<Chat> {
        val connection = postgresClient.getConnection()
        try {
            val sql = """
                SELECT id, name
                FROM chats
            """.trimIndent()

            connection.createStatement().use { statement ->
                val resultSet = statement.executeQuery(sql)
                val chats = mutableListOf<Chat>()

                while (resultSet.next()) {
                    chats.add(
                        Chat(
                            id = resultSet.getLong("id"),
                            name = resultSet.getString("name"),
                        )
                    )
                }
                return chats
            }
        } finally {
            connection.close()
        }
    }
}