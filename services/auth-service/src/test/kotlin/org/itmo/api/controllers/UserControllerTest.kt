package org.itmo.api.controllers

import io.mockk.*
import kotlin.test.*
import org.itmo.api.request.UserUpdateRequest
import org.itmo.model.Image
import org.itmo.model.User
import org.itmo.model.UserShow
import org.itmo.repository.ImageRepository
import org.itmo.repository.UserRepository
import java.time.Instant

class UserControllerTest {

    private lateinit var userRepository: UserRepository
    private lateinit var imageRepository: ImageRepository
    private lateinit var userController: UserController

    @BeforeTest
    fun setup() {
        userRepository = mockk()
        imageRepository = mockk()
        userController = UserController(userRepository, imageRepository)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getUserById should return user when found`() {
        val userId = 1L
        val user = User(userId, "testuser", "test@example.com", 0, null, null, "hash", null, Instant.now())

        every { userRepository.getUserById(userId) } returns user

        val result = userController.getUserById(userId)

        assertEquals(user, result)
        verify { userRepository.getUserById(userId) }
    }

    @Test
    fun `getUserById should return null when not found`() {
        val userId = 1L

        every { userRepository.getUserById(userId) } returns null

        val result = userController.getUserById(userId)

        assertNull(result)
    }

    @Test
    fun `getAllUsers should return list of users`() {
        val users = listOf(
            UserShow(1L, "user1", "user1@example.com", 0, null, Instant.now()),
            UserShow(2L, "user2", "user2@example.com", 0, null, Instant.now())
        )

        every { userRepository.getAllUsers() } returns users

        val result = userController.getAllUsers()

        assertEquals(users, result)
        verify { userRepository.getAllUsers() }
    }

    @Test
    fun `uploadAvatar should create image and update user`() {
        val userId = 1L
        val imageData = byteArrayOf(1, 2, 3)
        val contentType = "image/jpeg"
        val imageId = 10L
        val user = User(userId, "testuser", "test@example.com", 0, null, null, "hash", null, Instant.now())

        every { userRepository.getUserById(userId) } returns user
        every { imageRepository.createImage(imageData, contentType, userId) } returns imageId
        every { userRepository.updateAvatarReference(userId, imageId) } returns true

        val result = userController.uploadAvatar(userId, imageData, contentType)

        assertEquals(imageId, result)
        verify { imageRepository.createImage(imageData, contentType, userId) }
        verify { userRepository.updateAvatarReference(userId, imageId) }
    }

    @Test
    fun `uploadAvatar should delete old avatar when exists`() {
        val userId = 1L
        val imageData = byteArrayOf(1, 2, 3)
        val contentType = "image/jpeg"
        val oldImageId = 5L
        val newImageId = 10L
        val user = User(userId, "testuser", "test@example.com", 0, null, oldImageId, "hash", null, Instant.now())

        every { userRepository.getUserById(userId) } returns user
        every { imageRepository.deleteImage(oldImageId) } returns true
        every { imageRepository.createImage(imageData, contentType, userId) } returns newImageId
        every { userRepository.updateAvatarReference(userId, newImageId) } returns true

        val result = userController.uploadAvatar(userId, imageData, contentType)

        assertEquals(newImageId, result)
        verify { imageRepository.deleteImage(oldImageId) }
    }

    @Test
    fun `uploadAvatar should throw exception when user not found`() {
        val userId = 1L
        val imageData = byteArrayOf(1, 2, 3)
        val contentType = "image/jpeg"

        every { userRepository.getUserById(userId) } returns null

        assertFailsWith<IllegalArgumentException> {
            userController.uploadAvatar(userId, imageData, contentType)
        }
    }

    @Test
    fun `getAvatar should return image when found`() {
        val userId = 1L
        val imageId = 10L
        val image = Image(imageId, byteArrayOf(1, 2, 3), "image/jpeg", 3, userId, Instant.now())

        every { userRepository.getAvatarImageId(userId) } returns imageId
        every { imageRepository.getImageById(imageId) } returns image

        val result = userController.getAvatar(userId)

        assertEquals(image, result)
        verify { imageRepository.getImageById(imageId) }
    }

    @Test
    fun `getAvatar should return null when no avatar`() {
        val userId = 1L

        every { userRepository.getAvatarImageId(userId) } returns null

        val result = userController.getAvatar(userId)

        assertNull(result)
    }

    @Test
    fun `deleteAvatar should remove avatar`() {
        val userId = 1L
        val imageId = 10L

        every { userRepository.getAvatarImageId(userId) } returns imageId
        every { userRepository.updateAvatarReference(userId, null) } returns true
        every { imageRepository.deleteImage(imageId) } returns true

        val result = userController.deleteAvatar(userId)

        assertTrue(result)
        verify { userRepository.updateAvatarReference(userId, null) }
        verify { imageRepository.deleteImage(imageId) }
    }

    @Test
    fun `deleteAvatar should return false when no avatar`() {
        val userId = 1L

        every { userRepository.getAvatarImageId(userId) } returns null

        val result = userController.deleteAvatar(userId)

        assertFalse(result)
    }

    @Test
    fun `updateUser should update profile`() {
        val userId = 1L
        val request = UserUpdateRequest("newname", "newemail@example.com", "new bio", 1)

        every { userRepository.updateProfile(userId, request.bio, request.status, request.name, request.email) } returns true

        val result = userController.updateUser(userId, request)

        assertTrue(result)
        verify { userRepository.updateProfile(userId, request.bio, request.status, request.name, request.email) }
    }
}

