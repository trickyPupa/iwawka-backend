package org.itmo.api.controllers

import io.mockk.*
import kotlin.test.*
import org.itmo.api.request.LoginRequest
import org.itmo.api.request.RefreshRequest
import org.itmo.api.request.RegisterRequest
import org.itmo.service.AuthService
import org.itmo.service.AuthTokens

class AuthControllerTest {

    private lateinit var authService: AuthService
    private lateinit var authController: AuthController

    @BeforeTest
    fun setup() {
        authService = mockk()
        authController = AuthController(authService)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `register should return auth response`() {
        val request = RegisterRequest("testuser", "test@example.com", "password123")
        val userId = 1L
        val tokens = AuthTokens("access-token", "refresh-token", 3600L)

        every { authService.register(any(), any(), any()) } returns userId
        every { authService.issueTokens(userId) } returns tokens

        val result = authController.register(request)

        assertEquals("access-token", result.accessToken)
        assertEquals("refresh-token", result.refreshToken)
        assertEquals(3600L, result.expiresIn)
        verify { authService.register("testuser", "test@example.com", "password123") }
        verify { authService.issueTokens(userId) }
    }

    @Test
    fun `login should return auth response`() {
        val request = LoginRequest("test@example.com", "password123")
        val tokens = AuthTokens("access-token", "refresh-token", 3600L)

        every { authService.login(any(), any()) } returns tokens

        val result = authController.login(request)

        assertEquals("access-token", result.accessToken)
        assertEquals("refresh-token", result.refreshToken)
        assertEquals(3600L, result.expiresIn)
        verify { authService.login("test@example.com", "password123") }
    }

    @Test
    fun `refresh should return auth response`() {
        val request = RefreshRequest("refresh-token")
        val tokens = AuthTokens("new-access-token", "new-refresh-token", 3600L)

        every { authService.refresh(any()) } returns tokens

        val result = authController.refresh(request)

        assertEquals("new-access-token", result.accessToken)
        assertEquals("new-refresh-token", result.refreshToken)
        assertEquals(3600L, result.expiresIn)
        verify { authService.refresh("refresh-token") }
    }

    @Test
    fun `logout should revoke token`() {
        val request = RefreshRequest("refresh-token")

        every { authService.revoke(any()) } just Runs

        authController.logout(request)

        verify { authService.revoke("refresh-token") }
    }
}

