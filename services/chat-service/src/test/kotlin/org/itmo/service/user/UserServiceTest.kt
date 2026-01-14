package org.itmo.service.user

import io.mockk.*
import kotlin.test.*
import kotlinx.coroutines.runBlocking

class UserServiceTest {

    private lateinit var userClient: UserClient
    private lateinit var userCache: UserCache
    private lateinit var userService: UserService

    @BeforeTest
    fun setup() {
        userClient = mockk()
        userCache = mockk()
        userService = UserService(userClient, userCache)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getUsers should return empty map when ids is empty`() = runBlocking {
        val result = userService.getUsers(emptySet())

        assertTrue(result.isEmpty())
        verify(exactly = 0) { userCache.getMany(any()) }
        coVerify(exactly = 0) { userClient.getUsers(any()) }
    }

    @Test
    fun `getUsers should return cached users when all are cached`() = runBlocking {
        val ids = setOf(1L, 2L)
        val cachedUsers = mapOf(
            1L to RemoteUser(1L, "user1", "user1@example.com", 0, null, null),
            2L to RemoteUser(2L, "user2", "user2@example.com", 0, null, null)
        )

        every { userCache.getMany(ids) } returns cachedUsers

        val result = userService.getUsers(ids)

        assertEquals(cachedUsers, result)
        verify { userCache.getMany(ids) }
        coVerify(exactly = 0) { userClient.getUsers(any()) }
    }

    @Test
    fun `getUsers should fetch missing users and cache them`() = runBlocking {
        val ids = setOf(1L, 2L, 3L)
        val cachedUsers = mapOf(
            1L to RemoteUser(1L, "user1", "user1@example.com", 0, null, null)
        )
        val fetchedUsers = mapOf(
            2L to RemoteUser(2L, "user2", "user2@example.com", 0, null, null),
            3L to RemoteUser(3L, "user3", "user3@example.com", 0, null, null)
        )

        every { userCache.getMany(ids) } returns cachedUsers
        coEvery { userClient.getUsers(setOf(2L, 3L)) } returns fetchedUsers
        every { userCache.put(any()) } just Runs

        val result = userService.getUsers(ids)

        assertEquals(3, result.size)
        assertEquals(cachedUsers[1L], result[1L])
        assertEquals(fetchedUsers[2L], result[2L])
        assertEquals(fetchedUsers[3L], result[3L])
        verify { userCache.getMany(ids) }
        coVerify { userClient.getUsers(setOf(2L, 3L)) }
        verify(exactly = 2) { userCache.put(any()) }
    }
}

