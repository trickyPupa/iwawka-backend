package org.itmo.service.user

import io.mockk.*
import kotlin.test.*
import org.itmo.db.RedisClient

class UserCacheTest {

    private lateinit var redisClient: RedisClient
    private lateinit var userCache: UserCache

    @BeforeTest
    fun setup() {
        redisClient = mockk()
        userCache = UserCache(redisClient)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `put should store user in cache`() {
        val user = RemoteUser(1L, "testuser", "test@example.com", 0, null, null)
        val ttlSeconds = 600L

        every { redisClient.setex(any(), any(), ttlSeconds) } just Runs

        userCache.put(user, ttlSeconds)

        verify { redisClient.setex("user:1", any(), ttlSeconds) }
    }

    @Test
    fun `get should return user when found in cache`() {
        val userId = 1L
        val user = RemoteUser(userId, "testuser", "test@example.com", 0, null, null)
        val userJson = """{"id":1,"username":"testuser","email":"test@example.com","status":0}"""

        every { redisClient.get("user:$userId") } returns userJson

        val result = userCache.get(userId)

        assertNotNull(result)
        assertEquals(user.id, result?.id)
        assertEquals(user.username, result?.username)
        assertEquals(user.email, result?.email)
    }

    @Test
    fun `get should return null when not found in cache`() {
        val userId = 1L

        every { redisClient.get("user:$userId") } returns null

        val result = userCache.get(userId)

        assertNull(result)
    }

    @Test
    fun `getMany should return empty map when ids is empty`() {
        val result = userCache.getMany(emptySet())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getMany should return cached users`() {
        val ids = setOf(1L, 2L)
        val user1Json = """{"id":1,"username":"user1","email":"user1@example.com","status":0}"""
        val user2Json = """{"id":2,"username":"user2","email":"user2@example.com","status":0}"""

        every { redisClient.get("user:1") } returns user1Json
        every { redisClient.get("user:2") } returns user2Json

        val result = userCache.getMany(ids)

        assertEquals(2, result.size)
        assertEquals(1L, result[1L]?.id)
        assertEquals(2L, result[2L]?.id)
    }

    @Test
    fun `getMany should skip invalid JSON`() {
        val ids = setOf(1L, 2L)
        val user1Json = """{"id":1,"username":"user1","email":"user1@example.com","status":0}"""
        val invalidJson = "invalid json"

        every { redisClient.get("user:1") } returns user1Json
        every { redisClient.get("user:2") } returns invalidJson

        val result = userCache.getMany(ids)

        assertEquals(1, result.size)
        assertEquals(1L, result[1L]?.id)
    }
}

