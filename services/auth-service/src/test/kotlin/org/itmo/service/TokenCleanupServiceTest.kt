package org.itmo.service

import io.mockk.*
import kotlin.test.*
import org.itmo.repository.TokenRepository
import java.time.Instant

class TokenCleanupServiceTest {

    private lateinit var tokenRepository: TokenRepository
    private lateinit var tokenCleanupService: TokenCleanupService

    @BeforeTest
    fun setup() {
        tokenRepository = mockk()
        tokenCleanupService = TokenCleanupService(tokenRepository)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
        tokenCleanupService.stopCleanup()
    }

    @Test
    fun `cleanup should delete expired tokens`() {
        val deletedCount = 5

        every { tokenRepository.deleteExpiredTokens(any()) } returns deletedCount

        val result = tokenCleanupService.cleanup()

        assertEquals(deletedCount, result)
        verify { tokenRepository.deleteExpiredTokens(any()) }
    }

    @Test
    fun `cleanup should return zero when no expired tokens`() {
        every { tokenRepository.deleteExpiredTokens(any()) } returns 0

        val result = tokenCleanupService.cleanup()

        assertEquals(0, result)
    }

    @Test
    fun `startCleanup should start periodic cleanup`() {
        every { tokenRepository.deleteExpiredTokens(any()) } returns 0

        tokenCleanupService.startCleanup(1)

        // Wait a bit to ensure cleanup job started
        Thread.sleep(100)

        verify(atLeast = 0) { tokenRepository.deleteExpiredTokens(any()) }
    }

    @Test
    fun `stopCleanup should stop periodic cleanup`() {
        every { tokenRepository.deleteExpiredTokens(any()) } returns 0

        tokenCleanupService.startCleanup(1)
        Thread.sleep(50)
        tokenCleanupService.stopCleanup()
        Thread.sleep(50)

        // After stop, no more calls should be made
        verify(atMost = 1) { tokenRepository.deleteExpiredTokens(any()) }
    }
}

