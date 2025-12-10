package org.itmo.service

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import org.itmo.api.getPrincipalUserId
import org.itmo.model.*
import org.itmo.repository.AuditLogRepository
import java.time.Instant

/**
 * Сервис для логирования действий пользователей и системных событий
 */
class AuditLogService(
    private val auditLogRepository: AuditLogRepository = AuditLogRepository()
) {

    /**
     * Логировать действие пользователя
     */
    suspend fun logAction(
        userId: Long?,
        username: String?,
        action: AuditAction,
        entityType: EntityType? = null,
        entityId: Long? = null,
        details: String? = null,
        ipAddress: String? = null,
        userAgent: String? = null,
        success: Boolean = true,
        errorMessage: String? = null
    ) {
        val log = AuditLog(
            timestamp = Instant.now(),
            userId = userId,
            username = username,
            action = action.action,
            entityType = entityType?.type,
            entityId = entityId,
            details = details,
            ipAddress = ipAddress,
            userAgent = userAgent,
            success = success,
            errorMessage = errorMessage
        )
        auditLogRepository.saveAuditLog(log)
    }

    /**
     * Логировать HTTP запрос
     */
    suspend fun logRequest(
        method: String,
        uri: String,
        statusCode: Int,
        duration: Long,
        userId: Long? = null,
        username: String? = null,
        ipAddress: String? = null,
        userAgent: String? = null,
        requestBody: String? = null,
        responseBody: String? = null,
        errorMessage: String? = null
    ) {
        val log = RequestLog(
            timestamp = Instant.now(),
            method = method,
            uri = uri,
            statusCode = statusCode,
            duration = duration,
            userId = userId,
            username = username,
            ipAddress = ipAddress,
            userAgent = userAgent,
            requestBody = requestBody,
            responseBody = responseBody,
            errorMessage = errorMessage
        )
        auditLogRepository.saveRequestLog(log)
    }

    /**
     * Логировать операцию с базой данных
     */
    suspend fun logDatabaseOperation(
        operation: String,
        tableName: String,
        recordId: Long? = null,
        userId: Long? = null,
        username: String? = null,
        query: String? = null,
        duration: Long = 0,
        success: Boolean = true,
        errorMessage: String? = null,
        affectedRows: Int? = null
    ) {
        val log = DatabaseLog(
            timestamp = Instant.now(),
            operation = operation,
            tableName = tableName,
            recordId = recordId,
            userId = userId,
            username = username,
            query = query,
            duration = duration,
            success = success,
            errorMessage = errorMessage,
            affectedRows = affectedRows
        )
        auditLogRepository.saveDatabaseLog(log)
    }

    /**
     * Получить логи аудита
     */
    suspend fun getAuditLogs(
        startTime: Instant,
        endTime: Instant,
        userId: Long? = null,
        action: String? = null,
        limit: Int = 1000
    ): List<AuditLog> {
        return auditLogRepository.getAuditLogs(startTime, endTime, userId, action, limit)
    }

    /**
     * Получить статистику действий пользователя
     */
    suspend fun getUserActionStats(userId: Long, days: Int = 7): Map<String, Long> {
        return auditLogRepository.getUserActionStats(userId, days)
    }
}

/**
 * Extension функции для удобного логирования из ApplicationCall
 */
suspend fun ApplicationCall.logAudit(
    action: AuditAction,
    entityType: EntityType? = null,
    entityId: Long? = null,
    details: String? = null,
    success: Boolean = true,
    errorMessage: String? = null,
    auditLogService: AuditLogService
) {
    try {
        val userId = getPrincipalUserId()
        val username: String? = null
        val ipAddress = request.local.remoteHost
        val userAgent = request.headers["User-Agent"]

        auditLogService.logAction(
            userId = userId,
            username = username,
            action = action,
            entityType = entityType,
            entityId = entityId,
            details = details,
            ipAddress = ipAddress,
            userAgent = userAgent,
            success = success,
            errorMessage = errorMessage
        )
    } catch (e: Exception) {
        println("⚠️ Failed to log audit action: ${e.message}")
    }
}

