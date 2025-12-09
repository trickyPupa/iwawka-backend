package org.itmo

import org.itmo.api.controllers.ChatController
import org.itmo.api.configureRouting
import org.itmo.config.Config
import org.itmo.db.PostgresClient
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*
import io.ktor.server.response.*
import org.flywaydb.core.Flyway
import org.itmo.api.controllers.MessageController
import org.itmo.config.configureSwagger
import org.itmo.db.ClickHouseClient
import org.itmo.resources.MessageResource
import org.itmo.service.MessageService

fun main() {
    if (Config.flywayEnabled) {
        runMigrations()
    }

    embeddedServer(Netty, port = Config.serverPort, host = Config.serverHost) {
        module()
    }.start(wait = true)
}

fun runMigrations() {
    val maxRetries = 10
    var retries = 0
    var lastException: Exception? = null

    while (retries < maxRetries) {
        try {
            println("Attempting to connect to database... (attempt ${retries + 1}/$maxRetries)")
            Flyway.configure()
                .dataSource(Config.postgresUrl, Config.postgresUser, Config.postgresPassword)
                .locations(*Config.flywayLocations.toTypedArray())
                .load()
                .migrate()
            return
        } catch (e: Exception) {
            lastException = e
            retries++
            if (retries < maxRetries) {
                val waitTime = minOf(1000L * retries, 10000L)
                println("Failed to connect to database. Retrying in ${waitTime}ms...")
                Thread.sleep(waitTime)
            }
        }
    }

    throw RuntimeException("Failed to connect to database after $maxRetries attempts", lastException)
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
    val clickHouseClient = ClickHouseClient(
        url = Config.clickhouseUrl,
        user = Config.clickhouseUser,
        password = Config.clickhousePassword
    )

    val messageResource = MessageResource(postgresClient)

    val messageService = MessageService(messageResource)

    val messageController = MessageController(messageService)
    val chatController = ChatController()

    configureRouting(messageController, chatController)

//    configureSwagger()
}