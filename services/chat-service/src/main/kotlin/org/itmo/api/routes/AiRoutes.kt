package org.itmo.api.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.itmo.api.controllers.AiController
import org.itmo.api.logger
import org.itmo.api.request.SummarizeRequest
import org.itmo.api.respondError
import org.itmo.api.respondSuccess

fun Route.aiRoutes(aiController: AiController) {
    route("/ai") {
        post("/summarize") {
            try{
                val request = call.receive<SummarizeRequest>()
                if (request.interval != null) {
                    val result = aiController.summarizeByInterval(request.chatId, request.interval)
                    call.respondSuccess(mapOf("summarize" to result))
                } else if (request.messageIds != null) {
                    val result = aiController.summarizeByIds(request.messageIds)
                    call.respondSuccess(mapOf("summarize" to result))
                } else {
                    call.respondError("Message IDs either Interval cannot be empty")
                }
            } catch (e: Exception) {
                logger.error(e.stackTraceToString())
                call.respondError(e.message ?: "Unknown error", HttpStatusCode.InternalServerError)
            }
        }
    }
}