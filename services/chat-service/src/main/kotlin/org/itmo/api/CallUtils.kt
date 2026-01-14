package org.itmo.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
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
    return request.header("Authorization")?.removePrefix("Bearer ")
}

fun ApplicationCall.getPrincipalUserId(): Long? {
    return request.header("X-User-Id")?.toLongOrNull()
}

fun ApplicationCall.requirePrincipalUserId(): Long {
    return getPrincipalUserId()
        ?: throw IllegalStateException("User ID not found in request headers")
}

suspend fun ApplicationCall.requireAuth(): Long {
    return getPrincipalUserId() ?: throw IllegalArgumentException("Unauthorized")
}

suspend fun ApplicationCall.getCurrentUserId(): Long? {
    return getPrincipalUserId()
}
