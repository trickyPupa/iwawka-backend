package org.itmo.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.itmo.model.User
import org.itmo.repository.UserRepository
import org.slf4j.LoggerFactory

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

fun ApplicationCall.getPathParameter(name: String): String? {
    return parameters[name]
}

fun ApplicationCall.getAuthToken(): String? {
    return request.header("Authorization")?.removePrefix("Bearer ")
}

fun ApplicationCall.getPrincipalUserId(): Long? {
    val principal = principal<JWTPrincipal>()
    return principal?.getClaim("sub", String::class)?.toLongOrNull()
}

fun ApplicationCall.requirePrincipalUserId(): Long {
    return getPrincipalUserId() 
        ?: throw IllegalStateException("User ID not found in JWT token")
}

suspend fun ApplicationCall.requireAuth(userRepository: UserRepository): User {
    val userId = getPrincipalUserId()
        ?: throw IllegalArgumentException("Unauthorized")
    
    return userRepository.getUserById(userId)
        ?: throw IllegalArgumentException("User not found")
}

suspend fun ApplicationCall.getCurrentUser(userRepository: UserRepository): User? {
    val userId = getPrincipalUserId() ?: return null
    return userRepository.getUserById(userId)
}
