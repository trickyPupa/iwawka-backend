package org.itmo.api.routes

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray
import org.itmo.api.controllers.UserController
import org.itmo.api.getPathParameter
import org.itmo.api.respondError
import org.itmo.api.respondSuccess
import org.itmo.api.request.UserUpdateRequest

fun Route.userRoutes(userController: UserController) {
    route("/users") {
        get {
            try {
                val users = userController.getAllUsers()
                call.respondSuccess(users)
            } catch (e: Exception) {
                call.respondError(e.message ?: "",HttpStatusCode.BadRequest)
            }
        }

        get("/{id}") {
            try{
                val userId = call.getPathParameter("id")?.toLongOrNull() ?: run {
                    call.respondError("Invalid user ID", HttpStatusCode.BadRequest)
                    return@get
                }

                val user = userController.getUserById(userId)
                if (user != null) {
                    call.respondSuccess(user)
                } else {
                    call.respondError("User not found", HttpStatusCode.NotFound)
                }
            } catch (e: Exception) {
                call.respondError(e.message ?: "",HttpStatusCode.BadRequest)
            }
        }

        post("/{id}") {
            try {
                val userId = call.getPathParameter("id")?.toLongOrNull() ?: run {
                    call.respondError("Invalid user ID", HttpStatusCode.BadRequest)
                    return@post
                }

                val request = call.receive<UserUpdateRequest>()
                val updated = userController.updateUser(userId, request)

                if (updated) {
                    call.respondSuccess("Profile updated successfully")
                } else {
                    call.respondError("Failed to update profile", HttpStatusCode.InternalServerError)
                }
            } catch (e: Exception) {
                call.respondError("Invalid request: ${e.message}", HttpStatusCode.BadRequest)
            }
        }

        post("/{id}/avatar") {
            val userId = call.getPathParameter("id")?.toLongOrNull() ?: run {
                call.respondError("Invalid user ID", HttpStatusCode.BadRequest)
                return@post
            }

            val multipart = call.receiveMultipart()
            var imageBytes: ByteArray? = null
            var contentType: String? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        if (part.name == "avatar" || part.name == "file") {
                            imageBytes = part.provider().readRemaining().readByteArray()
                            contentType = part.contentType?.toString() ?: "image/jpeg"
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            if (imageBytes != null && imageBytes.isNotEmpty()) {
                try {
                    val imageId = userController.uploadAvatar(userId, imageBytes, contentType!!)
                    call.respond(HttpStatusCode.OK, mapOf(
                        "message" to "Avatar uploaded successfully",
                        "imageId" to imageId
                    ))
                } catch (e: IllegalArgumentException) {
                    call.respondError(e.message ?: "User not found", HttpStatusCode.NotFound)
                } catch (e: Exception) {
                    call.respondError("Failed to upload avatar: ${e.message}", HttpStatusCode.InternalServerError)
                }
            } else {
                call.respondError("No avatar file provided or file is empty", HttpStatusCode.BadRequest)
            }
        }

        get("/{id}/avatar") {
            val userId = call.getPathParameter("id")?.toLongOrNull() ?: run {
                call.respondError("Invalid user ID", HttpStatusCode.BadRequest)
                return@get
            }

            val image = userController.getAvatar(userId)
            if (image != null) {
                call.respondBytes(image.data, ContentType.parse(image.contentType))
            } else {
                call.respondError("Avatar not found", HttpStatusCode.NotFound)
            }
        }

        delete("/{id}/avatar") {
            val userId = call.getPathParameter("id")?.toLongOrNull() ?: run {
                call.respondError("Invalid user ID", HttpStatusCode.BadRequest)
                return@delete
            }

            val deleted = userController.deleteAvatar(userId)
            if (deleted) {
                call.respondSuccess("Avatar deleted")
            } else {
                call.respondError("Avatar not found or already deleted", HttpStatusCode.NotFound)
            }
        }
    }
}

