package org.itmo.api

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.itmo.api.controllers.*
import org.itmo.api.routes.*

fun Application.configureRouting(
    messageController: MessageController,
    chatController: ChatController,
    userController: UserController,
    imageController: ImageController,
    authController: AuthController
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
            authRoutes(authController)
            messageRoutes(messageController)
            chatRoutes(chatController)
            userRoutes(userController)
            imageRoutes(imageController)
        }
    }
}