package org.itmo.api.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.origin
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.coroutines.launch
import org.itmo.service.AuditLogService

/**
 * Плагин для автоматического логирования всех HTTP запросов
 */
val RequestLoggingPlugin = createApplicationPlugin(
    name = "RequestLogging",
    createConfiguration = ::RequestLoggingConfiguration
) {
    val auditLogService = pluginConfig.auditLogService
    val logRequestBody = pluginConfig.logRequestBody
    val logResponseBody = pluginConfig.logResponseBody
    val excludePaths = pluginConfig.excludePaths

    onCall { call ->
        val startTime = System.currentTimeMillis()
        val uri = call.request.uri

        if (excludePaths.any { uri.startsWith(it) }) {
            return@onCall
        }

        val method = call.request.httpMethod.value
        val ipAddress = call.request.origin.remoteHost
        val userAgent = call.request.headers["User-Agent"]

        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.payload?.getClaim("userId")?.asLong()
        val username = principal?.payload?.getClaim("username")?.asString()

        val requestBody = if (logRequestBody && (call.request.contentLength() ?: 0) > 0) {
            try {
                call.receiveText().take(1000) // Ограничиваем размер
            } catch (e: Exception) {
                null
            }
        } else null

        call.response.pipeline.intercept(ApplicationSendPipeline.After) { message ->
            val duration = System.currentTimeMillis() - startTime
            val statusCode = call.response.status()?.value ?: 0

            launch {
                auditLogService.logRequest(
                    method = method,
                    uri = uri,
                    statusCode = statusCode,
                    duration = duration,
                    userId = userId,
                    username = username,
                    ipAddress = ipAddress,
                    userAgent = userAgent,
                    requestBody = requestBody,
                    responseBody = null,
                    errorMessage = if (statusCode >= 400) "HTTP $statusCode" else null
                )
            }
        }
    }
}

class RequestLoggingConfiguration {
    var auditLogService: AuditLogService = AuditLogService()
    var logRequestBody: Boolean = false
    var logResponseBody: Boolean = false
    var excludePaths: List<String> = listOf("/health", "/metrics")
}

fun Application.configureRequestLogging(auditLogService: AuditLogService) {
    install(RequestLoggingPlugin) {
        this.auditLogService = auditLogService
        this.logRequestBody = false
        this.logResponseBody = false
        this.excludePaths = listOf("/health", "/metrics", "/swagger", "/openapi")
    }
}

