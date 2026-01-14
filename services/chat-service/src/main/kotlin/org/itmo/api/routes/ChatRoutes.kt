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
            try {
                val chats = chatController.getAllChatsEnriched()
                call.respondSuccess(chats)
            } catch (e: Exception) {
                call.respondError(e.message ?: "Failed to get chats", HttpStatusCode.InternalServerError)
            }
        }

        post {
            try {
                val request = call.receive<ChatCreateRequest>()
                val result = chatController.createChat(request.name, request.members)
                call.respondSuccess(mapOf("chatId" to result))
            } catch (e: Exception) {
                call.respondError(e.message ?: "Failed to create chat", HttpStatusCode.BadRequest)
            }
        }

        post("/{id}") {
            try {
                val chatId = call.getPathParameter("id")?.toLongOrNull() ?: run {
                    call.respondError("Invalid chat ID", HttpStatusCode.BadRequest)
                    return@post
                }
                val request = call.receive<AddUsersToChatRequest>()

                if (chatController.addUsers(chatId, request.userIds)) {
                    call.respondSuccess(mapOf("message" to "Users added successfully"))
                } else {
                    call.respondError("Failed to add users", HttpStatusCode.BadRequest)
                }
            } catch (e: Exception) {
                call.respondError(e.message ?: "Failed to add users", HttpStatusCode.BadRequest)
            }
        }
    }
}
