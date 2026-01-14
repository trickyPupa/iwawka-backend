package org.itmo.api.controllers

import io.mockk.*
import kotlin.test.*
import kotlinx.coroutines.runBlocking
import org.itmo.api.request.SendMessageRequest
import org.itmo.model.Message
import org.itmo.service.MessageService
import org.itmo.service.user.RemoteUser
import org.itmo.service.user.UserService
import java.time.Instant

class MessageControllerTest {

    private lateinit var messageService: MessageService
    private lateinit var userService: UserService
    private lateinit var messageController: MessageController

    @BeforeTest
    fun setup() {
        messageService = mockk()
        userService = mockk()
        messageController = MessageController(messageService, userService)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `sendMessage should return true when message sent`() {
        val request = SendMessageRequest("Hello", 1L)
        val userId = 1L

        every { messageService.sendMessage(request.text, request.chatId, userId) } returns true

        val result = messageController.sendMessage(request, userId)

        assertTrue(result)
        verify { messageService.sendMessage("Hello", 1L, userId) }
    }

    @Test
    fun `deleteMessage should return true when message deleted`() {
        val messageId = 1L

        every { messageService.deleteMessage(messageId) } returns true

        val result = messageController.deleteMessage(messageId)

        assertTrue(result)
        verify { messageService.deleteMessage(messageId) }
    }

    @Test
    fun `getByChat should return messages from service`() {
        val chatId = 1L
        val messages = listOf(
            Message(1L, "Message 1", 1L, chatId, Instant.now()),
            Message(2L, "Message 2", 2L, chatId, Instant.now())
        )

        every { messageService.getByChat(chatId) } returns messages

        val result = messageController.getByChat(chatId)

        assertEquals(messages, result)
        verify { messageService.getByChat(chatId) }
    }

    @Test
    fun `getByChatEnriched should return messages with user data`() = runBlocking {
        val chatId = 1L
        val userId1 = 1L
        val userId2 = 2L
        val messages = listOf(
            Message(1L, "Message 1", userId1, chatId, Instant.now()),
            Message(2L, "Message 2", userId2, chatId, Instant.now())
        )
        val users = mapOf(
            userId1 to RemoteUser(userId1, "user1", "user1@example.com", 0, null, null),
            userId2 to RemoteUser(userId2, "user2", "user2@example.com", 0, null, null)
        )

        every { messageService.getByChat(chatId) } returns messages
        coEvery { userService.getUsers(setOf(userId1, userId2)) } returns users

        val result = messageController.getByChatEnriched(chatId)

        assertTrue(result.containsKey("messages"))
        val items = result["messages"] as List<*>
        assertEquals(2, items.size)
        verify { messageService.getByChat(chatId) }
        coVerify { userService.getUsers(setOf(userId1, userId2)) }
    }
}

