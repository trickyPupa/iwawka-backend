package org.itmo.service

import io.mockk.*
import kotlin.test.*
import org.itmo.db.RedisClient

class AiServiceTest {

    private lateinit var redisClient: RedisClient
    private lateinit var aiService: AiService

    @BeforeTest
    fun setup() {
        try {
            redisClient = mockk(relaxed = true)
            aiService = AiService("test-api-key", redisClient)
        } catch (e: Exception) {
            // If HttpClient initialization fails, skip tests
            // This can happen in some test environments
            throw AssertionError("Failed to initialize AiService: ${e.message}", e)
        }
    }

    @AfterTest
    fun tearDown() {
        try {
            if (::aiService.isInitialized) {
                aiService.close()
            }
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
        clearAllMocks()
    }

    @Test
    fun `generateUID should return UUID string`() {
        val uid = aiService.generateUID()

        assertNotNull(uid)
        assertTrue(uid.isNotBlank())
    }

    @Test
    fun `generateUID should return different values`() {
        val uid1 = aiService.generateUID()
        val uid2 = aiService.generateUID()

        assertNotEquals(uid1, uid2)
    }
}

