package org.itmo.api.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.itmo.api.controllers.MessageController
import org.itmo.api.getPathParameter
import org.itmo.api.request.SendMessageRequest
import org.itmo.api.requirePrincipalUserId
import org.itmo.api.respondError
import org.itmo.api.respondSuccess

fun Route.messageRoutes(messageController: MessageController) {
    route("/message") {
        get("/{chatId}") {
            try {
                val chatId = call.getPathParameter("chatId")?.toLongOrNull() ?: run {
                    call.respondError("Required parameter \"chatId\" must be integer", HttpStatusCode.BadRequest)
                    return@get
                }

                call.respondSuccess(messageController.getByChatEnriched(chatId), HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respondError(e.message ?: "Failed to get messages", HttpStatusCode.InternalServerError)
            }
        }

        post {
            try {
                val userId = call.requirePrincipalUserId()

                val requestData = call.receive<SendMessageRequest>()
                val success = messageController.sendMessage(requestData, userId)

                if (success) {
                    call.respondSuccess(mapOf("message" to "Message sent successfully"))
                } else {
                    call.respondError("Failed to send message", HttpStatusCode.BadRequest)
                }
            } catch (e: Exception) {
                call.respondError(e.message ?: "Failed to send message", HttpStatusCode.BadRequest)
            }
        }

        delete("/{id}") {
            try {
                val messageId = call.getPathParameter("id")?.toLongOrNull() ?: run {
                    call.respondError("Required parameter \"id\" must be integer", HttpStatusCode.BadRequest)
                    return@delete
                }

                if (messageController.deleteMessage(messageId)) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            } catch (e: Exception) {
                call.respondError(e.message ?: "Failed to delete message", HttpStatusCode.InternalServerError)
            }
        }
    }
}
