package org.itmo.repository

import org.itmo.db.PostgresClient
import org.itmo.model.AuthToken
import java.sql.Timestamp
import java.time.Instant

class TokenRepository(private val postgresClient: PostgresClient) {

    fun saveToken(userId: Long, token: String, tokenType: String, expiresAt: Instant): Long {
        val connection = postgresClient.getConnection()
        try {
            val sql = """
                INSERT INTO user_tokens (user_id, token, token_type, expires_at)
                VALUES (?, ?, ?, ?)
                RETURNING id
            """.trimIndent()

            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, userId)
                statement.setString(2, token)
                statement.setString(3, tokenType)
                statement.setTimestamp(4, Timestamp.from(expiresAt))

                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    return resultSet.getLong("id")
                } else {
                    throw RuntimeException("Failed to persist token")
                }
            }
        } finally {
            connection.close()
        }
    }

    fun findToken(token: String, tokenType: String): AuthToken? {
        val connection = postgresClient.getConnection()
        try {
            val sql = """
                SELECT id, user_id, token, token_type, expires_at, created_at, revoked
                FROM user_tokens
                WHERE token = ? AND token_type = ?
            """.trimIndent()

            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, token)
                statement.setString(2, tokenType)
                val rs = statement.executeQuery()

                return if (rs.next()) {
                    AuthToken(
                        id = rs.getLong("id"),
                        userId = rs.getLong("user_id"),
                        token = rs.getString("token"),
                        tokenType = rs.getString("token_type"),
                        expiresAt = rs.getTimestamp("expires_at").toInstant(),
                        createdAt = rs.getTimestamp("created_at").toInstant(),
                        revoked = rs.getBoolean("revoked")
                    )
                } else {
                    null
                }
            }
        } finally {
            connection.close()
        }
    }

    fun revokeToken(token: String): Boolean {
        val connection = postgresClient.getConnection()
        try {
            val sql = "UPDATE user_tokens SET revoked = TRUE WHERE token = ?"
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, token)
                return statement.executeUpdate() > 0
            }
        } finally {
            connection.close()
        }
    }

    fun deleteExpiredTokens(now: Instant = Instant.now()): Int {
        val connection = postgresClient.getConnection()
        try {
            val sql = "DELETE FROM user_tokens WHERE expires_at < ?"
            connection.prepareStatement(sql).use { statement ->
                statement.setTimestamp(1, Timestamp.from(now))
                return statement.executeUpdate()
            }
        } finally {
            connection.close()
        }
    }

    fun revokeAllUserTokens(userId: Long): Int {
        val connection = postgresClient.getConnection()
        try {
            val sql = "UPDATE user_tokens SET revoked = TRUE WHERE user_id = ? AND revoked = FALSE"
            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, userId)
                return statement.executeUpdate()
            }
        } finally {
            connection.close()
        }
    }

    fun getUserTokens(userId: Long, tokenType: String? = null): List<AuthToken> {
        val connection = postgresClient.getConnection()
        try {
            val sql = if (tokenType != null) {
                """
                    SELECT id, user_id, token, token_type, expires_at, created_at, revoked
                    FROM user_tokens
                    WHERE user_id = ? AND token_type = ?
                    ORDER BY created_at DESC
                """.trimIndent()
            } else {
                """
                    SELECT id, user_id, token, token_type, expires_at, created_at, revoked
                    FROM user_tokens
                    WHERE user_id = ?
                    ORDER BY created_at DESC
                """.trimIndent()
            }

            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, userId)
                if (tokenType != null) {
                    statement.setString(2, tokenType)
                }

                val rs = statement.executeQuery()
                val tokens = mutableListOf<AuthToken>()

                while (rs.next()) {
                    tokens.add(
                        AuthToken(
                            id = rs.getLong("id"),
                            userId = rs.getLong("user_id"),
                            token = rs.getString("token"),
                            tokenType = rs.getString("token_type"),
                            expiresAt = rs.getTimestamp("expires_at").toInstant(),
                            createdAt = rs.getTimestamp("created_at").toInstant(),
                            revoked = rs.getBoolean("revoked")
                        )
                    )
                }

                return tokens
            }
        } finally {
            connection.close()
        }
    }
}

