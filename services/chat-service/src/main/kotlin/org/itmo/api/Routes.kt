package org.itmo.api

import io.ktor.http.ContentType
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.itmo.api.controllers.*
import org.itmo.api.routes.*
import org.itmo.service.AuditLogService

fun Application.configureRouting(
    messageController: MessageController,
    chatController: ChatController,
    auditLogService: AuditLogService,
    testController: LoadTestController,
    aiController: AiController,
    prometheusRegistry: PrometheusMeterRegistry
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

        get("/metrics") {
            call.respondText(prometheusRegistry.scrape(), ContentType.parse("text/plain; version=0.0.4"))
        }

        route("/api") {
            messageRoutes(messageController)
            chatRoutes(chatController)
            auditLogRoutes(auditLogService)
            aiRoutes(aiController)
        }
    }
}