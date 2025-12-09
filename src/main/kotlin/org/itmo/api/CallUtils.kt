package org.itmo.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.itmo.model.User

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)

suspend fun <T> ApplicationCall.respondSuccess(data: T, status: HttpStatusCode = HttpStatusCode.OK) {
    respond(status, ApiResponse(success = true, data = data))
}

suspend fun ApplicationCall.respondError(
    message: String,
    status: HttpStatusCode = HttpStatusCode.BadRequest
) {
    respond(status, ApiResponse<Unit>(success = false, error = message))
}

suspend fun ApplicationCall.getPathParameter(name: String): String? {
    return parameters[name] ?: run {
        respondError("Missing required parameter: $name", HttpStatusCode.BadRequest)
        null
    }
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

