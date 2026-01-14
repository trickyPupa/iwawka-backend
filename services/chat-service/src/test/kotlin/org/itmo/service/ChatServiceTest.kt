package org.itmo.service

import io.mockk.*
import kotlin.test.*
import org.itmo.db.PostgresClient

class ChatServiceTest {

    private lateinit var postgresClient: PostgresClient
    private lateinit var chatService: ChatService

    @BeforeTest
    fun setup() {
        postgresClient = mockk()
        chatService = ChatService(postgresClient)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `createChat should return true`() {
        val chatName = "Test Chat"
        val memberIds = listOf("1", "2")

        val result = chatService.createChat(chatName, memberIds)

        assertTrue(result)
    }
}

