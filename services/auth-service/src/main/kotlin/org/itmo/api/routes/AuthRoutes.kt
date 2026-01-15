package org.itmo.api.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import kotlinx.coroutines.launch
import org.itmo.api.controllers.AuthController
import org.itmo.api.getPrincipalUserId
import org.itmo.api.request.LoginRequest
import org.itmo.api.request.RefreshRequest
import org.itmo.api.request.RegisterRequest
import org.itmo.api.respondSuccess
import org.itmo.config.JwtConfig
import org.itmo.model.AuditAction
import org.itmo.service.AuditLogService

/**
 * Маршруты аутентификации с интеграцией аудит-логирования
 */
fun Route.authRoutes(authController: AuthController, auditLogService: AuditLogService? = null) {
    route("/auth") {
        get("/validate") {
            val authHeader = call.request.headers["Authorization"]

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Missing or invalid Authorization header"))
                return@get
            }

            try {
                val token = authHeader.substring(7)
                val jwt = JwtConfig.verifier.verify(token)
                val userId = jwt.subject

                call.response.header("X-User-Id", userId)
                call.respond(HttpStatusCode.OK, mapOf("valid" to true, "userId" to userId))
            } catch (_: Exception) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
            }
        }

        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()
                val response = authController.register(request)

                val userId = JwtConfig.extractUserId(response.accessToken)

                // Логирование успешной регистрации
                auditLogService?.let { service ->
                    application.launch {
                        service.logAction(
                            userId = userId,
                            username = request.username,
                            action = AuditAction.REGISTER,
                            details = "New user registered: ${request.username}",
                            ipAddress = call.request.local.remoteHost,
                            userAgent = call.request.headers["User-Agent"],
                            success = true
                        )
                    }
                }

                call.respondSuccess(response)
            } catch (e: Exception) {
                // Логирование неудачной регистрации
                auditLogService?.let { service ->
                    application.launch {
                        service.logAction(
                            userId = null,
                            username = null,
                            action = AuditAction.REGISTER,
                            details = "Failed registration attempt",
                            ipAddress = call.request.local.remoteHost,
                            userAgent = call.request.headers["User-Agent"],
                            success = false,
                            errorMessage = e.message
                        )
                    }
                }
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        post("/login") {
            val loginRequest = try { call.receive<LoginRequest>() } catch (_: Exception) { null }
            try {
                if (loginRequest == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request body"))
                    return@post
                }

                val response = authController.login(loginRequest)
                val userId = JwtConfig.extractUserId(response.accessToken)

                // Логирование успешного входа
                auditLogService?.let { service ->
                    application.launch {
                        service.logAction(
                            userId = userId,
                            username = loginRequest.email,
                            action = AuditAction.LOGIN,
                            details = "Successful login",
                            ipAddress = call.request.local.remoteHost,
                            userAgent = call.request.headers["User-Agent"],
                            success = true
                        )
                    }
                }

                call.respondSuccess(response)
            } catch (e: Exception) {
                // Логирование неудачной попытки входа
                auditLogService?.let { service ->
                    application.launch {
                        service.logAction(
                            userId = null,
                            username = loginRequest?.email,
                            action = AuditAction.UNAUTHORIZED_ACCESS,
                            details = "Failed login attempt",
                            ipAddress = call.request.local.remoteHost,
                            userAgent = call.request.headers["User-Agent"],
                            success = false,
                            errorMessage = e.message
                        )
                    }
                }
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
            }
        }

        authenticate("auth-jwt") {
            post("/refresh") {
                try {
                    val request = call.receive<RefreshRequest>()
                    val response = authController.refresh(request)
                    val userId = call.getPrincipalUserId()
                    val username = null

                    // Логирование обновления токена
                    auditLogService?.let { service ->
                        application.launch {
                            service.logAction(
                                userId = userId,
                                username = username,
                                action = AuditAction.TOKEN_REFRESH,
                                details = "Token refreshed successfully",
                                ipAddress = call.request.local.remoteHost,
                                userAgent = call.request.headers["User-Agent"],
                                success = true
                            )
                        }
                    }

                    call.respondSuccess(response)
                } catch (e: Exception) {
                    val userId = call.getPrincipalUserId()
                    val username = null
                    auditLogService?.let { service ->
                        application.launch {
                            service.logAction(
                                userId = userId,
                                username = username,
                                action = AuditAction.TOKEN_REFRESH,
                                details = "Failed to refresh token",
                                ipAddress = call.request.local.remoteHost,
                                userAgent = call.request.headers["User-Agent"],
                                success = false,
                                errorMessage = e.message
                            )
                        }
                    }
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to e.message))
                }
            }

            post("/logout") {
                try {
                    val request = call.receive<RefreshRequest>()
                    authController.logout(request)
                    val userId = call.getPrincipalUserId()
                    val username = null

                    auditLogService?.let { service ->
                        application.launch {
                            service.logAction(
                                userId = userId,
                                username = username,
                                action = AuditAction.LOGOUT,
                                details = "User logged out",
                                ipAddress = call.request.local.remoteHost,
                                userAgent = call.request.headers["User-Agent"],
                                success = true
                            )
                        }
                    }

                    call.respondSuccess(mapOf("message" to "Logged out successfully"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }
        }
    }
}

