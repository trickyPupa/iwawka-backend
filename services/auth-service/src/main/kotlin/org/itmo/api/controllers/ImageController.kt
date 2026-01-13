package org.itmo.api.controllers

import org.itmo.model.Image
import org.itmo.model.ImageMetadata
import org.itmo.repository.ImageRepository

class ImageController(private val imageRepository: ImageRepository) {

    /**
     * Получает изображение по ID
     */
    fun getImageById(imageId: Long): Image? {
        return imageRepository.getImageById(imageId)
    }

    /**
     * Получает метаданные изображения
     */
    fun getImageMetadata(imageId: Long): ImageMetadata? {
        return imageRepository.getImageMetadata(imageId)
    }

    /**
     * Загружает новое изображение
     */
    fun uploadImage(imageData: ByteArray, contentType: String, uploadedBy: Long?): Long {
        return imageRepository.createImage(imageData, contentType, uploadedBy)
    }

    /**
     * Удаляет изображение
     */
    fun deleteImage(imageId: Long): Boolean {
        return imageRepository.deleteImage(imageId)
    }

    /**
     * Получает все изображения пользователя
     */
    fun getUserImages(userId: Long): List<ImageMetadata> {
        return imageRepository.getImagesByUser(userId)
    }
}

