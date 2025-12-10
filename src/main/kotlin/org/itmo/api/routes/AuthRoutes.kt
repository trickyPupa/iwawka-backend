package org.itmo.api.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.itmo.api.controllers.AuthController
import org.itmo.api.request.LoginRequest
import org.itmo.api.request.RefreshRequest
import org.itmo.api.request.RegisterRequest
import org.itmo.api.respondError
import org.itmo.api.respondSuccess

fun Route.authRoutes(authController: AuthController) {
    route("/auth") {
        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()
                val response = authController.register(request)
                call.respondSuccess(response, HttpStatusCode.Created)
            } catch (e: Exception) {
                call.respondError(e.message ?: "Registration failed", HttpStatusCode.BadRequest)
            }
        }

        post("/login") {
            try {
                val request = call.receive<LoginRequest>()
                val response = authController.login(request)
                call.respondSuccess(response)
            } catch (e: Exception) {
                call.respondError(e.message ?: "Login failed", HttpStatusCode.BadRequest)
            }
        }

        post("/refresh") {
            try {
                val request = call.receive<RefreshRequest>()
                val response = authController.refresh(request)
                call.respondSuccess(response)
            } catch (e: Exception) {
                call.respondError(e.message ?: "Refresh failed", HttpStatusCode.BadRequest)
            }
        }

        post("/logout") {
            try {
                val request = call.receive<RefreshRequest>()
                authController.logout(request)
                call.respondSuccess(mapOf("message" to "Logged out"))
            } catch (e: Exception) {
                call.respondError(e.message ?: "Logout failed", HttpStatusCode.BadRequest)
            }
        }
    }
}

