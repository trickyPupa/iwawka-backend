package org.itmo.api.controllers

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.itmo.api.getPrincipalUserId
import org.itmo.api.respondError
import org.itmo.model.AuditAction
import org.itmo.model.EntityType
import org.itmo.service.AuditLogService
import org.itmo.service.logAudit
import java.time.Instant
import java.time.temporal.ChronoUnit

fun Route.auditLogRoutes(auditLogService: AuditLogService) {

    route("/audit") {
        authenticate("auth-jwt") {

            /**
             * GET /api/audit/logs
             * Получить логи аудита за период
             */
            get("/logs") {
                try {
                    val daysParam = call.request.queryParameters["days"]?.toIntOrNull() ?: 7
                    val userIdParam = call.request.queryParameters["userId"]?.toLongOrNull()
                    val actionParam = call.request.queryParameters["action"]
                    val limitParam = call.request.queryParameters["limit"]?.toIntOrNull() ?: 1000

                    val endTime = Instant.now()
                    val startTime = endTime.minus(daysParam.toLong(), ChronoUnit.DAYS)

                    val logs = auditLogService.getAuditLogs(
                        startTime = startTime,
                        endTime = endTime,
                        userId = userIdParam,
                        action = actionParam,
                        limit = limitParam
                    )

                    call.logAudit(
                        action = AuditAction.USER_LIST,
                        details = "Retrieved ${logs.size} audit logs",
                        auditLogService = auditLogService
                    )

                    call.respond(HttpStatusCode.OK, mapOf(
                        "logs" to logs,
                        "count" to logs.size,
                        "startTime" to startTime.toString(),
                        "endTime" to endTime.toString()
                    ))
                } catch (e: Exception) {
                    call.logAudit(
                        action = AuditAction.SYSTEM_ERROR,
                        success = false,
                        errorMessage = e.message,
                        auditLogService = auditLogService
                    )
                    call.respondError("Failed to retrieve audit logs ${e.message}", HttpStatusCode.InternalServerError)
                }
            }

            /**
             * GET /api/audit/stats
             * Получить статистику действий текущего пользователя
             */
            get("/stats") {
                try {
                    val userId = call.getPrincipalUserId()
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "User not authenticated"))
                        return@get
                    }

                    val days = call.request.queryParameters["days"]?.toIntOrNull() ?: 7
                    val stats = auditLogService.getUserActionStats(userId, days)

                    call.logAudit(
                        action = AuditAction.USER_VIEW,
                        details = "Retrieved user action stats",
                        auditLogService = auditLogService
                    )

                    call.respond(HttpStatusCode.OK, mapOf(
                        "userId" to userId,
                        "days" to days,
                        "stats" to stats
                    ))
                } catch (e: Exception) {
                    call.logAudit(
                        action = AuditAction.SYSTEM_ERROR,
                        success = false,
                        errorMessage = e.message,
                        auditLogService = auditLogService
                    )
                    call.respond(HttpStatusCode.InternalServerError, mapOf(
                        "error" to "Failed to retrieve stats",
                        "message" to e.message
                    ))
                }
            }

            /**
             * GET /api/audit/user/{userId}/stats
             * Получить статистику действий конкретного пользователя (для администраторов)
             */
            get("/user/{userId}/stats") {
                try {
                    val targetUserId = call.parameters["userId"]?.toLongOrNull()
                    if (targetUserId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user ID"))
                        return@get
                    }

                    val days = call.request.queryParameters["days"]?.toIntOrNull() ?: 7
                    val stats = auditLogService.getUserActionStats(targetUserId, days)

                    call.logAudit(
                        action = AuditAction.USER_VIEW,
                        entityType = EntityType.USER,
                        entityId = targetUserId,
                        details = "Retrieved user action stats for user $targetUserId",
                        auditLogService = auditLogService
                    )

                    call.respond(HttpStatusCode.OK, mapOf(
                        "userId" to targetUserId,
                        "days" to days,
                        "stats" to stats
                    ))
                } catch (e: Exception) {
                    call.logAudit(
                        action = AuditAction.SYSTEM_ERROR,
                        success = false,
                        errorMessage = e.message,
                        auditLogService = auditLogService
                    )
                    call.respond(HttpStatusCode.InternalServerError, mapOf(
                        "error" to "Failed to retrieve stats",
                        "message" to e.message
                    ))
                }
            }
        }
    }
}

