package org.itmo.repository

import org.itmo.db.PostgresClient
import org.itmo.model.User
import java.sql.Timestamp
import java.time.Instant

class UserRepository(private val postgresClient: PostgresClient) {

    /**
     * Получает пользователя по ID
     */
    fun getUserById(userId: Long): User? {
        val connection = postgresClient.getConnection()
        try {
            val sql = """
                SELECT id, username, email, status, bio, image_id, created_at
                FROM users
                WHERE id = ?
            """.trimIndent()

            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, userId)
                val resultSet = statement.executeQuery()

                return if (resultSet.next()) {
                    User(
                        id = resultSet.getLong("id"),
                        username = resultSet.getString("username"),
                        email = resultSet.getString("email"),
                        status = resultSet.getInt("status"),
                        bio = resultSet.getString("bio"),
                        imageId = resultSet.getObject("image_id") as Long?,
                        createdAt = resultSet.getTimestamp("created_at").toInstant()
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            throw e
        } finally {
            connection.close()
        }
    }

    /**
     * Обновляет аватар пользователя
     * @param userId ID пользователя
     * @param imageId ID изображения из таблицы images
     * @return true, если обновление прошло успешно
     */
    fun updateAvatarReference(userId: Long, imageId: Long?): Boolean {
        val connection = postgresClient.getConnection()
        try {
            val sql = "UPDATE users SET image_id = ? WHERE id = ?"
            connection.prepareStatement(sql).use { statement ->
                if (imageId != null) {
                    statement.setLong(1, imageId)
                } else {
                    statement.setNull(1, java.sql.Types.BIGINT)
                }
                statement.setLong(2, userId)
                val rowsAffected = statement.executeUpdate()
                return rowsAffected > 0
            }
        } catch (e: Exception) {
            throw e
        } finally {
            connection.close()
        }
    }

    /**
     * Получает ID аватара пользователя
     */
    fun getAvatarImageId(userId: Long): Long? {
        val connection = postgresClient.getConnection()
        try {
            val sql = "SELECT image_id FROM users WHERE id = ?"
            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, userId)
                val resultSet = statement.executeQuery()

                return if (resultSet.next()) {
                    resultSet.getObject("image_id") as Long?
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            throw e
        } finally {
            connection.close()
        }
    }

    /**
     * Создает нового пользователя
     */
    fun createUser(username: String, email: String): Long {
        val connection = postgresClient.getConnection()
        try {
            val sql = """
                INSERT INTO users (username, email, status, created_at)
                VALUES (?, ?, 0, ?)
                RETURNING id
            """.trimIndent()

            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, username)
                statement.setString(2, email)
                statement.setTimestamp(3, Timestamp.from(Instant.now()))

                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    return resultSet.getLong("id")
                } else {
                    throw RuntimeException("Failed to create user")
                }
            }
        } catch (e: Exception) {
            throw e
        } finally {
            connection.close()
        }
    }

    /**
     * Обновляет профиль пользователя
     */
    fun updateProfile(userId: Long, bio: String?, status: Int?, username: String?, email: String?): Boolean {
        val connection = postgresClient.getConnection()
        try {
            val updates = mutableListOf<String>()
            if (bio != null) updates.add("bio = '$bio'")
            if (status != null) updates.add("status = '$status'")
            if (username != null) updates.add("username = '$username'")
            if (email != null) updates.add("email = '$email'")

            if (updates.isEmpty()) return false

            val sql = "UPDATE users SET ${updates.joinToString(", ")} WHERE id = '$userId'"

            connection.prepareStatement(sql).use { statement ->
                val rowsAffected = statement.executeUpdate()
                return rowsAffected > 0
            }
        } catch (e: Exception) {
            throw e
        } finally {
            connection.close()
        }
    }

    /**
     * Получает всех пользователей
     */
    fun getAllUsers(): List<User> {
        val connection = postgresClient.getConnection()
        try {
            val sql = """
                SELECT id, username, email, status, bio, created_at
                FROM users
                ORDER BY created_at DESC
            """.trimIndent()

            connection.createStatement().use { statement ->
                val resultSet = statement.executeQuery(sql)
                val users = mutableListOf<User>()

                while (resultSet.next()) {
                    users.add(
                        User(
                            id = resultSet.getLong("id"),
                            username = resultSet.getString("username"),
                            email = resultSet.getString("email"),
                            status = resultSet.getInt("status"),
                            bio = resultSet.getString("bio"),
                            createdAt = resultSet.getTimestamp("created_at").toInstant(),
                        )
                    )
                }
                return users
            }
        } catch (e: Exception) {
            throw e
        } finally {
            connection.close()
        }
    }
}