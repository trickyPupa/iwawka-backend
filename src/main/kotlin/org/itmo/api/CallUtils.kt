package org.itmo.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.itmo.model.User
import org.itmo.repository.ChatRepository
import org.slf4j.LoggerFactory

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)

val logger = LoggerFactory.getLogger(ChatRepository::class.java)

suspend fun <T> ApplicationCall.respondSuccess(data: T, status: HttpStatusCode = HttpStatusCode.OK) {
    respond(status, ApiResponse(success = true, data = data))
}

suspend fun ApplicationCall.respondError(
    message: String,
    status: HttpStatusCode = HttpStatusCode.BadRequest
) {
    logger.error("Error: $message")
    respond(status, ApiResponse<Unit>(success = false, error = message))
}

fun ApplicationCall.getPathParameter(name: String): String? {
    return parameters[name]
}

fun ApplicationCall.getAuthToken(): String? {
    return this.request.header("Authorization")?.removePrefix("Bearer ")
}

fun ApplicationCall.getAuthUser(): User? {
    val token = this.request.header("Authorization")?.removePrefix("Bearer ")

    if (token.isNullOrEmpty()) {
        return null
    }
    else TODO()
}

suspend fun ApplicationCall.requireAuth(): String? {
    val token = getAuthToken()
    if (token == null) {
        respondError("Unauthorized", HttpStatusCode.Unauthorized)
        return null
    }
    // TODO: валидация токена
    return token
}

