package org.itmo

import api.configureRouting
import api.handlers.MessageHandler
import config.Config
import db.PostgresClient
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*
import io.ktor.server.response.*
import org.flywaydb.core.Flyway
import service.MessageService

fun main() {
    if (Config.flywayEnabled) {
        Flyway.configure()
            .dataSource(Config.postgresUrl, Config.postgresUser, Config.postgresPassword)
            .locations(*Config.flywayLocations.toTypedArray())
            .load()
            .migrate()
    }

    embeddedServer(Netty, port = Config.serverPort, host = Config.serverHost) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        jackson()
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.localizedMessage)
        }
    }

    val postgresClient = PostgresClient(
        url = Config.postgresUrl,
        user = Config.postgresUser,
        password = Config.postgresPassword
    )
    val messageService = MessageService(postgresClient)
    val messageHandler = MessageHandler(messageService)

    configureRouting(messageHandler)
}