package org.itmo.api.routes

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray
import org.itmo.api.controllers.ImageController
import org.itmo.api.getPathParameter
import org.itmo.api.getPrincipalUserId
import org.itmo.api.respondError
import org.itmo.api.respondSuccess

fun Route.imageRoutes(imageController: ImageController) {
    route("/images") {
        // Публичный доступ для получения изображений
        get("/{id}") {
            try {
                val imageId = call.getPathParameter("id")?.toLongOrNull() ?: run {
                    call.respondError("Invalid image ID", HttpStatusCode.BadRequest)
                    return@get
                }

                val image = imageController.getImageById(imageId)
                if (image != null) {
                    call.respondBytes(image.data, ContentType.parse(image.contentType))
                } else {
                    call.respondError("Image not found", HttpStatusCode.NotFound)
                }
            } catch (e: Exception) {
                call.respondError(e.message ?: "Failed to get image", HttpStatusCode.InternalServerError)
            }
        }

        get("/{id}/metadata") {
            try {
                val imageId = call.getPathParameter("id")?.toLongOrNull() ?: run {
                    call.respondError("Invalid image ID", HttpStatusCode.BadRequest)
                    return@get
                }

                val metadata = imageController.getImageMetadata(imageId)
                if (metadata != null) {
                    call.respond(HttpStatusCode.OK, metadata)
                } else {
                    call.respondError("Image not found", HttpStatusCode.NotFound)
                }
            } catch (e: Exception) {
                call.respondError(e.message ?: "Failed to get metadata", HttpStatusCode.InternalServerError)
            }
        }

        // Защищённые маршруты для загрузки и удаления
        authenticate("auth-jwt") {
            post {
                try {
                    val userId = call.getPrincipalUserId() ?: run {
                        call.respondError("Unauthorized", HttpStatusCode.Unauthorized)
                        return@post
                    }

                    val multipart = call.receiveMultipart()
                    var imageBytes: ByteArray? = null
                    var contentType: String? = null

                    multipart.forEachPart { part ->
                        when (part) {
                            is PartData.FileItem -> {
                                if (part.name == "image" || part.name == "file") {
                                    imageBytes = part.provider().readRemaining().readByteArray()
                                    contentType = part.contentType?.toString() ?: "image/jpeg"
                                }
                            }
                            else -> {}
                        }
                        part.dispose()
                    }

                    if (imageBytes != null && imageBytes.isNotEmpty()) {
                        val imageId = imageController.uploadImage(imageBytes, contentType!!, userId)
                        call.respond(HttpStatusCode.Created, mapOf(
                            "message" to "Image uploaded successfully",
                            "imageId" to imageId
                        ))
                    } else {
                        call.respondError("No image file provided or file is empty", HttpStatusCode.BadRequest)
                    }
                } catch (e: Exception) {
                    call.respondError("Failed to upload image: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }

            delete("/{id}") {
                try {
                    val imageId = call.getPathParameter("id")?.toLongOrNull() ?: run {
                        call.respondError("Invalid image ID", HttpStatusCode.BadRequest)
                        return@delete
                    }

                    val deleted = imageController.deleteImage(imageId)
                    if (deleted) {
                        call.respondSuccess("Image deleted successfully")
                    } else {
                        call.respondError("Image not found or already deleted", HttpStatusCode.NotFound)
                    }
                } catch (e: Exception) {
                    call.respondError(e.message ?: "Failed to delete image", HttpStatusCode.InternalServerError)
                }
            }
        }

        // Получить все изображения пользователя
        get("/user/{userId}") {
            val userId = call.getPathParameter("userId")?.toLongOrNull() ?: run {
                call.respondError("Invalid user ID", HttpStatusCode.BadRequest)
                return@get
            }

            val images = imageController.getUserImages(userId)
            call.respond(HttpStatusCode.OK, images)
        }
    }
}

