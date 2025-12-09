package org.itmo.api

import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.itmo.api.controllers.ChatController
import org.itmo.api.controllers.MessageController
import org.itmo.api.routes.chatRoutes
import org.itmo.api.routes.messageRoutes

fun Application.configureRouting(messageController: MessageController, chatController: ChatController) {
    routing {
        messageRoutes(messageController)
        chatRoutes(chatController)
    }
}