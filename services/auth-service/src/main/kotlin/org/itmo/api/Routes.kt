package org.itmo.api

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.itmo.api.controllers.*
import org.itmo.api.routes.*
import org.itmo.service.AuditLogService

fun Application.configureRouting(
    userController: UserController,
    imageController: ImageController,
    authController: AuthController,
    auditLogService: AuditLogService
) {
    routing {
        get("/") {
            call.respond(mapOf(
                "status" to "OK",
                "service" to "iwawka-backend",
                "version" to "0.0.1"
            ))
        }

        get("/health") {
            call.respond(mapOf("status" to "healthy"))
        }

        route("/api") {
            authRoutes(authController, auditLogService)
            userRoutes(userController)
            imageRoutes(imageController)
            auditLogRoutes(auditLogService)
        }
    }
}