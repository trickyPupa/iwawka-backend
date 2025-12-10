package org.itmo.api.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.*
import org.itmo.api.controllers.ChatController
import org.itmo.api.getPathParameter
import org.itmo.api.request.AddUsersToChatRequest
import org.itmo.api.request.ChatCreateRequest
import org.itmo.api.respondError
import org.itmo.api.respondSuccess

fun Route.chatRoutes(chatController: ChatController) {
    route("/chat") {
        get {
            val chats = chatController.getAllChats()
            call.respondSuccess(mapOf("chats" to chats))
        }

        post {
            val request = call.receive<ChatCreateRequest>()

            val result = chatController.createChat(request.name, request.members)
            call.respondSuccess(mapOf("chatId" to result))
        }

        post("/{id}") {
            val chatId = call.getPathParameter("id")?.toLongOrNull() ?: run {
                call.respondError("Invalid chat ID", HttpStatusCode.BadRequest)
                return@post
            }
            val request = call.receive<AddUsersToChatRequest>()

            if (chatController.addUsers(chatId, request.userIds)) {
                call.respondSuccess(null)
            } else {
                call.respondError("Error", HttpStatusCode.BadRequest)
            }
        }
    }
}

