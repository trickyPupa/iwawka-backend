package org.itmo.repository

import org.itmo.db.ClickHouseConnection
import org.itmo.model.AuditLog
import org.itmo.model.RequestLog
import org.itmo.model.DatabaseLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Timestamp

class AuditLogRepository {

    /**
     * Сохранить лог аудита в ClickHouse
     */
    suspend fun saveAuditLog(log: AuditLog) = withContext(Dispatchers.IO) {
        try {
            ClickHouseConnection.getConnection().use { connection ->
                val sql = """
                    INSERT INTO audit_logs (
                        timestamp, userId, username, action, entityType, entityId,
                        details, ipAddress, userAgent, success, errorMessage
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent()

                connection.prepareStatement(sql).use { statement ->
                    statement.setTimestamp(1, Timestamp.from(log.timestamp))
                    statement.setObject(2, log.userId)
                    statement.setString(3, log.username)
                    statement.setString(4, log.action)
                    statement.setString(5, log.entityType)
                    statement.setObject(6, log.entityId)
                    statement.setString(7, log.details)
                    statement.setString(8, log.ipAddress)
                    statement.setString(9, log.userAgent)
                    statement.setBoolean(10, log.success)
                    statement.setString(11, log.errorMessage)
                    statement.executeUpdate()
                }
            }
        } catch (e: Exception) {
            // Не бросаем исключение, чтобы не нарушать основной флоу
            println("⚠️ Failed to save audit log: ${e.message}")
        }
    }

    /**
     * Сохранить лог HTTP запроса в ClickHouse
     */
    suspend fun saveRequestLog(log: RequestLog) = withContext(Dispatchers.IO) {
        try {
            ClickHouseConnection.getConnection().use { connection ->
                val sql = """
                    INSERT INTO request_logs (
                        timestamp, method, uri, statusCode, duration, userId, username,
                        ipAddress, userAgent, requestBody, responseBody, errorMessage
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent()

                connection.prepareStatement(sql).use { statement ->
                    statement.setTimestamp(1, Timestamp.from(log.timestamp))
                    statement.setString(2, log.method)
                    statement.setString(3, log.uri)
                    statement.setInt(4, log.statusCode)
                    statement.setLong(5, log.duration)
                    statement.setObject(6, log.userId)
                    statement.setString(7, log.username)
                    statement.setString(8, log.ipAddress)
                    statement.setString(9, log.userAgent)
                    statement.setString(10, log.requestBody)
                    statement.setString(11, log.responseBody)
                    statement.setString(12, log.errorMessage)
                    statement.executeUpdate()
                }
            }
        } catch (e: Exception) {
            println("⚠️ Failed to save request log: ${e.message}")
        }
    }

    /**
     * Сохранить лог операции с базой данных в ClickHouse
     */
    suspend fun saveDatabaseLog(log: DatabaseLog) = withContext(Dispatchers.IO) {
        try {
            ClickHouseConnection.getConnection().use { connection ->
                val sql = """
                    INSERT INTO database_logs (
                        timestamp, operation, tableName, recordId, userId, username,
                        query, duration, success, errorMessage, affectedRows
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent()

                connection.prepareStatement(sql).use { statement ->
                    statement.setTimestamp(1, Timestamp.from(log.timestamp))
                    statement.setString(2, log.operation)
                    statement.setString(3, log.tableName)
                    statement.setObject(4, log.recordId)
                    statement.setObject(5, log.userId)
                    statement.setString(6, log.username)
                    statement.setString(7, log.query)
                    statement.setLong(8, log.duration)
                    statement.setBoolean(9, log.success)
                    statement.setString(10, log.errorMessage)
                    statement.setObject(11, log.affectedRows)
                    statement.executeUpdate()
                }
            }
        } catch (e: Exception) {
            println("⚠️ Failed to save database log: ${e.message}")
        }
    }

    /**
     * Получить логи аудита за период
     */
    suspend fun getAuditLogs(
        startTime: java.time.Instant,
        endTime: java.time.Instant,
        userId: Long? = null,
        action: String? = null,
        limit: Int = 1000
    ): List<AuditLog> = withContext(Dispatchers.IO) {
        val logs = mutableListOf<AuditLog>()
        try {
            ClickHouseConnection.getConnection().use { connection ->
                val conditions = mutableListOf("timestamp BETWEEN ? AND ?")
                if (userId != null) conditions.add("userId = ?")
                if (action != null) conditions.add("action = ?")

                val sql = """
                    SELECT * FROM audit_logs
                    WHERE ${conditions.joinToString(" AND ")}
                    ORDER BY timestamp DESC
                    LIMIT ?
                """.trimIndent()

                connection.prepareStatement(sql).use { statement ->
                    var paramIndex = 1
                    statement.setTimestamp(paramIndex++, Timestamp.from(startTime))
                    statement.setTimestamp(paramIndex++, Timestamp.from(endTime))
                    if (userId != null) statement.setLong(paramIndex++, userId)
                    if (action != null) statement.setString(paramIndex++, action)
                    statement.setInt(paramIndex, limit)

                    val rs = statement.executeQuery()
                    while (rs.next()) {
                        logs.add(
                            AuditLog(
                                timestamp = rs.getTimestamp("timestamp").toInstant(),
                                userId = rs.getObject("userId") as? Long,
                                username = rs.getString("username"),
                                action = rs.getString("action"),
                                entityType = rs.getString("entityType"),
                                entityId = rs.getObject("entityId") as? Long,
                                details = rs.getString("details"),
                                ipAddress = rs.getString("ipAddress"),
                                userAgent = rs.getString("userAgent"),
                                success = rs.getBoolean("success"),
                                errorMessage = rs.getString("errorMessage")
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            println("⚠️ Failed to fetch audit logs: ${e.message}")
        }
        logs
    }

    /**
     * Получить статистику по действиям пользователя
     */
    suspend fun getUserActionStats(userId: Long, days: Int = 7): Map<String, Long> = withContext(Dispatchers.IO) {
        val stats = mutableMapOf<String, Long>()
        try {
            ClickHouseConnection.getConnection().use { connection ->
                val sql = """
                    SELECT action, count() as count
                    FROM audit_logs
                    WHERE userId = ? AND timestamp >= now() - INTERVAL ? DAY
                    GROUP BY action
                    ORDER BY count DESC
                """.trimIndent()

                connection.prepareStatement(sql).use { statement ->
                    statement.setLong(1, userId)
                    statement.setInt(2, days)

                    val rs = statement.executeQuery()
                    while (rs.next()) {
                        stats[rs.getString("action")] = rs.getLong("count")
                    }
                }
            }
        } catch (e: Exception) {
            println("⚠️ Failed to fetch user action stats: ${e.message}")
        }
        stats
    }
}

