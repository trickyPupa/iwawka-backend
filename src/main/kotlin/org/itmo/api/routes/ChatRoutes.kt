package org.itmo.api.routes

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.itmo.api.controllers.ChatController

fun Route.chatRoutes(chatController: ChatController) {
    route("/chat") {
        get {
            call.respond(HttpStatusCode.OK, "List of chats")
        }

        post("/create") {
            val result = chatController.createChat(
                TODO(),
                memberIds = TODO()
            )
            call.respond(HttpStatusCode.Created, mapOf("success" to result))
        }
    }
}

