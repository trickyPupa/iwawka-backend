package org.itmo.api

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.itmo.api.controllers.*
import org.itmo.api.routes.*
import org.itmo.service.AuditLogService

fun Application.configureRouting(
    messageController: MessageController,
    chatController: ChatController,
    userController: UserController,
    imageController: ImageController,
    authController: AuthController,
    auditLogService: AuditLogService,
    testController: LoadTestController,
    aiController: AiController
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

        get("/load_test") {
            try {
                call.respondSuccess(testController.test())
            } catch (e: Exception) {
                call.respondError("error: ${e.message}")
            }
        }

        route("/api") {
            authRoutes(authController, auditLogService)
            messageRoutes(messageController)
            chatRoutes(chatController)
            userRoutes(userController)
            imageRoutes(imageController)
            auditLogRoutes(auditLogService)
            aiRoutes(aiController)
        }
    }
}