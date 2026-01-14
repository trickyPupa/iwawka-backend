package org.itmo.api.controllers

import io.mockk.*
import kotlin.test.*
import org.itmo.model.Image
import org.itmo.model.ImageMetadata
import org.itmo.repository.ImageRepository
import java.time.Instant

class ImageControllerTest {

    private lateinit var imageRepository: ImageRepository
    private lateinit var imageController: ImageController

    @BeforeTest
    fun setup() {
        imageRepository = mockk()
        imageController = ImageController(imageRepository)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getImageById should return image when found`() {
        val imageId = 1L
        val image = Image(imageId, byteArrayOf(1, 2, 3), "image/jpeg", 3, 1L, Instant.now())

        every { imageRepository.getImageById(imageId) } returns image

        val result = imageController.getImageById(imageId)

        assertEquals(image, result)
        verify { imageRepository.getImageById(imageId) }
    }

    @Test
    fun `getImageById should return null when not found`() {
        val imageId = 1L

        every { imageRepository.getImageById(imageId) } returns null

        val result = imageController.getImageById(imageId)

        assertNull(result)
    }

    @Test
    fun `getImageMetadata should return metadata when found`() {
        val imageId = 1L
        val metadata = ImageMetadata(imageId, "image/jpeg", 3, 1L, Instant.now())

        every { imageRepository.getImageMetadata(imageId) } returns metadata

        val result = imageController.getImageMetadata(imageId)

        assertEquals(metadata, result)
        verify { imageRepository.getImageMetadata(imageId) }
    }

    @Test
    fun `uploadImage should create and return image id`() {
        val imageData = byteArrayOf(1, 2, 3)
        val contentType = "image/jpeg"
        val uploadedBy = 1L
        val imageId = 10L

        every { imageRepository.createImage(imageData, contentType, uploadedBy) } returns imageId

        val result = imageController.uploadImage(imageData, contentType, uploadedBy)

        assertEquals(imageId, result)
        verify { imageRepository.createImage(imageData, contentType, uploadedBy) }
    }

    @Test
    fun `deleteImage should return true when deleted`() {
        val imageId = 1L

        every { imageRepository.deleteImage(imageId) } returns true

        val result = imageController.deleteImage(imageId)

        assertTrue(result)
        verify { imageRepository.deleteImage(imageId) }
    }

    @Test
    fun `getUserImages should return list of metadata`() {
        val userId = 1L
        val images = listOf(
            ImageMetadata(1L, "image/jpeg", 100, userId, Instant.now()),
            ImageMetadata(2L, "image/png", 200, userId, Instant.now())
        )

        every { imageRepository.getImagesByUser(userId) } returns images

        val result = imageController.getUserImages(userId)

        assertEquals(images, result)
        verify { imageRepository.getImagesByUser(userId) }
    }
}

