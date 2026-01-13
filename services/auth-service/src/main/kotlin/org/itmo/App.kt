package org.itmo

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import io.ktor.http.*
import io.ktor.server.response.*
import org.slf4j.event.Level
import org.flywaydb.core.Flyway
import org.itmo.config.Config
import org.itmo.config.JwtConfig
import org.itmo.api.configureRouting
import org.itmo.api.controllers.*
import org.itmo.api.plugins.configureRequestLogging
import org.itmo.repository.*
import org.itmo.service.*
import org.itmo.db.*

fun main() {
    if (Config.flywayEnabled) {
        runMigrations()
    }

    // Инициализация таблиц ClickHouse
    try {
        println("=== Initializing ClickHouse ===")
        ClickHouseConnection.initializeTables()
    } catch (e: Exception) {
        println("⚠️ Warning: Failed to initialize ClickHouse tables: ${e.message}")
        println("Application will continue, but logging to ClickHouse may not work")
    }

    embeddedServer(Netty, port = Config.serverPort, host = Config.serverHost) {
        module()
    }.start(wait = true)
}

fun runMigrations() {
    val maxRetries = 3
    var retries = 0
    var lastException: Exception? = null
    val cleanOnError = System.getenv("FLYWAY_CLEAN_ON_ERROR")?.toBoolean() ?: false

    while (retries < maxRetries) {
        try {
            println("=== Migration Attempt ${retries + 1}/$maxRetries ===")
            println("Database URL: ${Config.postgresUrl}")
            if (cleanOnError) {
                println("⚠️  FLYWAY_CLEAN_ON_ERROR is enabled - will drop all objects on error")
            }

            val flyway = Flyway.configure()
                .dataSource(Config.postgresUrl, Config.postgresUser, Config.postgresPassword)
                .locations(*Config.flywayLocations.toTypedArray())
                .baselineOnMigrate(true)
                .cleanDisabled(!cleanOnError)
                .load()

            val info = flyway.info()
            println("\n--- Current Migration Status ---")
            println("Current schema version: ${info.current()?.version ?: "none"}")
            println("Pending migrations: ${info.pending().size}")
            info.pending().forEach { migration ->
                println("  - ${migration.version}: ${migration.description}")
            }

            val result = flyway.migrate()

            println("\n--- Migration Result ---")
            println("✅ Migrations executed: ${result.migrationsExecuted}")
            println("✅ Database migrations completed successfully\n")
            return

        } catch (e: org.flywaydb.core.api.exception.FlywayValidateException) {
            println("\n❌ Flyway Validation Error:")
            println("Error message: ${e.message}")

            if (e.message?.contains("checksum mismatch", ignoreCase = true) == true) {
                println("\n⚠️  Migration file was modified after being applied to database")

                if (cleanOnError) {
                    try {
                        val flyway = Flyway.configure()
                            .dataSource(Config.postgresUrl, Config.postgresUser, Config.postgresPassword)
                            .locations(*Config.flywayLocations.toTypedArray())
                            .cleanDisabled(false)
                            .load()

                        flyway.clean()
                        println("✅ Database cleaned successfully")

                        flyway.migrate()
                        println("✅ Migrations completed successfully\n")
                        return
                    } catch (cleanException: Exception) {
                        println("❌ Clean and migrate failed: ${cleanException.message}")
                        cleanException.printStackTrace()
                        lastException = cleanException
                    }
                } else {
                    println("🔧 Attempting to repair schema history...")
                    try {
                        val flyway = Flyway.configure()
                            .dataSource(Config.postgresUrl, Config.postgresUser, Config.postgresPassword)
                            .locations(*Config.flywayLocations.toTypedArray())
                            .load()

                        flyway.repair()
                        println("✅ Schema history repaired successfully")

                        flyway.migrate()
                        println("✅ Migrations completed after repair\n")
                        return
                    } catch (repairException: Exception) {
                        println("❌ Repair failed: ${repairException.message}")
                        println("\n💡 Solution: Set FLYWAY_CLEAN_ON_ERROR=true to automatically clean DB on errors")
                        lastException = repairException
                    }
                }
            } else {
                lastException = e
            }

        } catch (e: java.sql.SQLException) {
            println("\n❌ Database Connection Error:")
            println("SQL State: ${e.sqlState}")
            println("Error Code: ${e.errorCode}")
            println("Error message: ${e.message}")
            lastException = e

        } catch (e: Exception) {
            println("\n❌ Unexpected Error:")
            println("Error type: ${e::class.simpleName}")
            println("Error message: ${e.message}")
            e.cause?.let { cause ->
                println("Caused by: ${cause.javaClass.simpleName} - ${cause.message}")
            }

            // Если это ошибка миграции и включен cleanOnError
            if (cleanOnError && e.message?.contains("migration", ignoreCase = true) == true) {
                println("\n🧹 Cleaning database and retrying from scratch...")
                try {
                    val flyway = Flyway.configure()
                        .dataSource(Config.postgresUrl, Config.postgresUser, Config.postgresPassword)
                        .locations(*Config.flywayLocations.toTypedArray())
                        .cleanDisabled(false)
                        .load()

                    flyway.clean()
                    println("✅ Database cleaned successfully")

                    flyway.migrate()
                    println("✅ Migrations completed successfully\n")
                    return
                } catch (cleanException: Exception) {
                    println("❌ Clean and migrate failed: ${cleanException.message}")
                    cleanException.printStackTrace()
                    lastException = cleanException
                }
            } else {
                e.printStackTrace()
                lastException = e
            }
        }

        retries++
        if (retries < maxRetries) {
            val waitTime = minOf(2000L * retries, 10000L)
            println("\n⏳ Retrying in ${waitTime / 1000}s...\n")
            Thread.sleep(waitTime)
        }
    }

    println("\n💥 Failed to complete migrations")
    println("Last error: ${lastException?.message}")
    throw RuntimeException("Failed to complete migrations", lastException)
}

fun Application.module() {
    install(CallLogging) {
        level = Level.INFO
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val uri = call.request.uri
            val userAgent = call.request.headers["User-Agent"]
            val contentType = call.request.contentType()

            buildString {
                append("$httpMethod $uri -> ${status?.value ?: "N/A"}")
                if (contentType != ContentType.Any) {
                    append(" | Content-Type: $contentType")
                }
                userAgent?.let { append(" | User-Agent: ${it.take(50)}") }
            }
        }
        filter { call ->
            !call.request.path().startsWith("/health")
        }
    }

    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JwtConfig.verifier)
            realm = Config.authJwtIssuer

            validate { credential ->
                // Проверяем, что токен содержит subject и audience
                if (credential.payload.subject != null &&
                    credential.payload.audience.contains(Config.authJwtAudience)) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf(
                        "success" to false,
                        "error" to "Invalid token"
                    )
                )
            }
        }
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

    val userRepository = UserRepository(postgresClient)
    val imageRepository = ImageRepository(postgresClient)
    val tokenRepository = TokenRepository(postgresClient)

    val authService = AuthService(userRepository, tokenRepository)
    val tokenCleanupService = TokenCleanupService(tokenRepository)
    val auditLogService = AuditLogService()

    tokenCleanupService.startCleanup(intervalHours = 24)

    val userController = UserController(userRepository, imageRepository)
    val imageController = ImageController(imageRepository)
    val authController = AuthController(authService)

    configureRequestLogging(auditLogService)

    configureRouting(userController, imageController, authController,
        auditLogService)
}