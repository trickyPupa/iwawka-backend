package org.itmo.service

import io.mockk.*
import kotlin.test.*
import org.itmo.model.User
import org.itmo.repository.TokenRepository
import org.itmo.repository.UserRepository
import java.time.Instant
import org.mindrot.jbcrypt.BCrypt

class AuthServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var tokenRepository: TokenRepository
    private lateinit var authService: AuthService

    @BeforeTest
    fun setup() {
        userRepository = mockk()
        tokenRepository = mockk()
        authService = AuthService(userRepository, tokenRepository)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `register should create user with valid data`() {
        val username = "testuser"
        val email = "test@example.com"
        val password = "password123"
        val userId = 1L

        every { userRepository.getUserByEmail(email) } returns null
        every { userRepository.createUser(any(), any(), any()) } returns userId
        every { userRepository.updateLastLogin(any(), any()) } returns true
        every { tokenRepository.saveToken(any(), any(), any(), any()) } returns 1L

        val result = authService.register(username, email, password)

        assertEquals(userId, result)
        verify { userRepository.getUserByEmail(email) }
        verify { userRepository.createUser(username, email, any()) }
        verify { userRepository.updateLastLogin(userId, any()) }
    }

    @Test
    fun `register should throw exception when email already exists`() {
        val username = "testuser"
        val email = "test@example.com"
        val password = "password123"
        val existingUser = User(1L, "existing", email, 0, null, null, "hash", null, Instant.now())

        every { userRepository.getUserByEmail(email) } returns existingUser

        assertFailsWith<IllegalArgumentException> {
            authService.register(username, email, password)
        }

        verify(exactly = 0) { userRepository.createUser(any(), any(), any()) }
    }

    @Test
    fun `register should throw exception when email is invalid`() {
        val username = "testuser"
        val email = "invalid-email"
        val password = "password123"

        every { userRepository.getUserByEmail(email) } returns null

        assertFailsWith<IllegalArgumentException> {
            authService.register(username, email, password)
        }
    }

    @Test
    fun `register should throw exception when password is too short`() {
        val username = "testuser"
        val email = "test@example.com"
        val password = "short"

        every { userRepository.getUserByEmail(email) } returns null

        assertFailsWith<IllegalArgumentException> {
            authService.register(username, email, password)
        }
    }

    @Test
    fun `login should return tokens with valid credentials`() {
        val email = "test@example.com"
        val password = "password123"
        val userId = 1L
        val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
        val user = User(userId, "testuser", email, 0, null, null, passwordHash, null, Instant.now())

        every { userRepository.getUserByEmail(email) } returns user
        every { userRepository.updateLastLogin(userId, any()) } returns true
        every { tokenRepository.saveToken(any(), any(), any(), any()) } returns 1L

        val result = authService.login(email, password)

        assertNotNull(result.accessToken)
        assertNotNull(result.refreshToken)
        assertTrue(result.expiresIn > 0)
        verify { userRepository.updateLastLogin(userId, any()) }
    }

    @Test
    fun `login should throw exception when user not found`() {
        val email = "test@example.com"
        val password = "password123"

        every { userRepository.getUserByEmail(email) } returns null

        assertFailsWith<IllegalArgumentException> {
            authService.login(email, password)
        }
    }

    @Test
    fun `login should throw exception when password is incorrect`() {
        val email = "test@example.com"
        val password = "wrongpassword"
        val userId = 1L
        val passwordHash = BCrypt.hashpw("correctpassword", BCrypt.gensalt())
        val user = User(userId, "testuser", email, 0, null, null, passwordHash, null, Instant.now())

        every { userRepository.getUserByEmail(email) } returns user

        assertFailsWith<IllegalArgumentException> {
            authService.login(email, password)
        }
    }

    @Test
    fun `refresh should return new tokens with valid refresh token`() {
        val userId = 1L
        val refreshToken = "valid-refresh-token"
        val expiresAt = Instant.now().plusSeconds(3600)
        val token = org.itmo.model.AuthToken(1L, userId, refreshToken, "refresh", expiresAt, Instant.now(), false)

        every { tokenRepository.findToken(refreshToken, "refresh") } returns token
        every { tokenRepository.revokeToken(refreshToken) } returns true
        every { tokenRepository.saveToken(any(), any(), any(), any()) } returns 1L

        val result = authService.refresh(refreshToken)

        assertNotNull(result.accessToken)
        assertNotNull(result.refreshToken)
        verify { tokenRepository.revokeToken(refreshToken) }
    }

    @Test
    fun `refresh should throw exception when token not found`() {
        val refreshToken = "invalid-token"

        every { tokenRepository.findToken(refreshToken, "refresh") } returns null

        assertFailsWith<IllegalArgumentException> {
            authService.refresh(refreshToken)
        }
    }

    @Test
    fun `refresh should throw exception when token is revoked`() {
        val userId = 1L
        val refreshToken = "revoked-token"
        val expiresAt = Instant.now().plusSeconds(3600)
        val token = org.itmo.model.AuthToken(1L, userId, refreshToken, "refresh", expiresAt, Instant.now(), true)

        every { tokenRepository.findToken(refreshToken, "refresh") } returns token

        assertFailsWith<IllegalArgumentException> {
            authService.refresh(refreshToken)
        }
    }

    @Test
    fun `refresh should throw exception when token is expired`() {
        val userId = 1L
        val refreshToken = "expired-token"
        val expiresAt = Instant.now().minusSeconds(3600)
        val token = org.itmo.model.AuthToken(1L, userId, refreshToken, "refresh", expiresAt, Instant.now(), false)

        every { tokenRepository.findToken(refreshToken, "refresh") } returns token

        assertFailsWith<IllegalArgumentException> {
            authService.refresh(refreshToken)
        }
    }

    @Test
    fun `revoke should revoke refresh token`() {
        val refreshToken = "valid-token"
        val userId = 1L
        val expiresAt = Instant.now().plusSeconds(3600)
        val token = org.itmo.model.AuthToken(1L, userId, refreshToken, "refresh", expiresAt, Instant.now(), false)

        every { tokenRepository.findToken(refreshToken, "refresh") } returns token
        every { tokenRepository.revokeToken(refreshToken) } returns true

        authService.revoke(refreshToken)

        verify { tokenRepository.revokeToken(refreshToken) }
    }

    @Test
    fun `revoke should throw exception when token not found`() {
        val refreshToken = "invalid-token"

        every { tokenRepository.findToken(refreshToken, "refresh") } returns null

        assertFailsWith<IllegalArgumentException> {
            authService.revoke(refreshToken)
        }
    }

    @Test
    fun `revokeAllUserTokens should revoke all user tokens`() {
        val userId = 1L
        val revokedCount = 3

        every { tokenRepository.revokeAllUserTokens(userId) } returns revokedCount

        val result = authService.revokeAllUserTokens(userId)

        assertEquals(revokedCount, result)
        verify { tokenRepository.revokeAllUserTokens(userId) }
    }

    @Test
    fun `issueTokens should generate access and refresh tokens`() {
        val userId = 1L

        every { tokenRepository.saveToken(any(), any(), any(), any()) } returns 1L

        val result = authService.issueTokens(userId)

        assertNotNull(result.accessToken)
        assertNotNull(result.refreshToken)
        assertTrue(result.expiresIn > 0)
        verify { tokenRepository.saveToken(userId, any(), "refresh", any()) }
    }
}

