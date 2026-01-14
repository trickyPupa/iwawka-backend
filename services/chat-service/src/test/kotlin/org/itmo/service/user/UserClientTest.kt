package org.itmo.service.user

import kotlin.test.*
import kotlinx.coroutines.runBlocking

class UserClientTest {

    private lateinit var userClient: UserClient

    @BeforeTest
    fun setup() {
        // Use a non-existent URL to avoid actual network calls during tests
        userClient = UserClient("http://localhost:9999")
    }

    @Test
    fun `getUsers should return empty map when userIds is empty`() = runBlocking {
        val result = userClient.getUsers(emptySet())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getUsers should handle network errors gracefully`() = runBlocking {
        // Since we can't easily mock HTTP client without dependency injection,
        // we'll test that the method doesn't crash on errors and returns empty map
        val userIds = setOf(1L, 2L)

        val result = userClient.getUsers(userIds)
        // Method should handle errors gracefully and return empty map
        assertNotNull(result)
        assertTrue(result.isEmpty())
    }
}

