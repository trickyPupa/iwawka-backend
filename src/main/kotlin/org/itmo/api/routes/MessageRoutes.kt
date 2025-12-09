package org.itmo.api.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.itmo.api.controllers.MessageController
import org.itmo.api.getAuthToken
import org.itmo.api.getPathParameter
import org.itmo.api.request.SendMessageRequest
import org.itmo.api.respondError
import org.itmo.api.respondSuccess

fun Route.messageRoutes(messageController: MessageController) {
    route("/message") {
        get("/{chatId}") {
            val chatId = call.getPathParameter("id")?.toLongOrNull() ?: run {
                call.respondError("Required parameter \"chatId\" must be integer", HttpStatusCode.BadRequest)
                return@get
            }

            call.respondSuccess(messageController.getByChat(chatId), HttpStatusCode.OK)
        }

        post {
            val requestData = call.receive<SendMessageRequest>()
            call.getAuthToken()

            val user = TODO()

            messageController.sendMessage(requestData, user)
            call.respondSuccess(HttpStatusCode.OK)
        }

        delete("/{id}") {
            val messageId = call.getPathParameter("id")?.toLongOrNull() ?: run {
                call.respondError("Required parameter \"id\" must be integer", HttpStatusCode.BadRequest)
                return@delete
            }
            if (messageController.deleteMessage(messageId)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

