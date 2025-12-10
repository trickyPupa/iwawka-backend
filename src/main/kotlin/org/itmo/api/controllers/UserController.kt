package org.itmo.api.controllers

import org.itmo.api.request.UserUpdateRequest
import org.itmo.model.Image
import org.itmo.model.User
import org.itmo.repository.ImageRepository
import org.itmo.repository.UserRepository

class UserController(
    private val userRepository: UserRepository,
    private val imageRepository: ImageRepository
) {

    /**
     * Получает пользователя по ID
     */
    fun getUserById(userId: Long): User? {
        return userRepository.getUserById(userId)
    }

    /**
     * Получает всех пользователей
     */
    fun getAllUsers(): List<User> {
        return userRepository.getAllUsers()
    }

    /**
     * Загружает аватар для пользователя
     * @return ID загруженного изображения
     */
    fun uploadAvatar(userId: Long, imageData: ByteArray, contentType: String): Long {
        val user = userRepository.getUserById(userId)
            ?: throw IllegalArgumentException("User with id $userId not found")

        user.imageId?.let { oldImageId ->
            imageRepository.deleteImage(oldImageId)
        }

        // Создаем новое изображение
        val imageId = imageRepository.createImage(imageData, contentType, userId)

        // Обновляем ссылку на аватар в профиле пользователя
        userRepository.updateAvatarReference(userId, imageId)

        return imageId
    }

    /**
     * Получает аватар пользователя
     */
    fun getAvatar(userId: Long): Image? {
        val imageId = userRepository.getAvatarImageId(userId) ?: return null
        return imageRepository.getImageById(imageId)
    }

    /**
     * Удаляет аватар пользователя
     */
    fun deleteAvatar(userId: Long): Boolean {
        val imageId = userRepository.getAvatarImageId(userId) ?: return false

        userRepository.updateAvatarReference(userId, null)

        return imageRepository.deleteImage(imageId)
    }

    /**
     * Обновляет профиль пользователя
     */
    fun updateUser(userId: Long, request: UserUpdateRequest): Boolean {
        return userRepository.updateProfile(userId, request.bio, request.status, request.name, request.email)
    }

    /**
     * Создает нового пользователя
     */
    fun createUser(username: String, email: String): Long {
        return userRepository.createUser(username, email)
    }
}