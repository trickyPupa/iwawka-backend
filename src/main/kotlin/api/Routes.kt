package api

import api.handlers.MessageHandler
import model.Message
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(messageHandler: MessageHandler) {
    routing {
        route("/messages") {
            get {
                call.respond(HttpStatusCode.OK, messageHandler.getAllMessages())
            }
            post {
                val message = call.receive<Message>()
                messageHandler.sendMessage(message)
                call.respond(HttpStatusCode.Created)
            }
            delete("/{id}") {
                val messageId = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                if (messageHandler.deleteMessage(messageId)) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }

        route("/users") {
            get {
                call.respond(HttpStatusCode.OK, messageHandler.getAllUsers())
            }
        }
    }
}