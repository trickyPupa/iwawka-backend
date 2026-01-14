package org.itmo.api.controllers

import io.mockk.*
import kotlin.test.*
import org.itmo.model.Chat
import org.itmo.repository.ChatRepository
import org.itmo.service.user.UserService

class ChatControllerTest {

    private lateinit var chatRepository: ChatRepository
    private lateinit var userService: UserService
    private lateinit var chatController: ChatController

    @BeforeTest
    fun setup() {
        chatRepository = mockk()
        userService = mockk()
        chatController = ChatController(chatRepository, userService)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `createChat should create chat and add members`() {
        val chatName = "Test Chat"
        val memberIds = listOf(1L, 2L)
        val chatId = 10L

        every { chatRepository.createChat(chatName) } returns chatId
        every { chatRepository.addUserToChat(chatId, any()) } returns true

        val result = chatController.createChat(chatName, memberIds)

        assertEquals(chatId, result)
        verify { chatRepository.createChat(chatName) }
        verify { chatRepository.addUserToChat(chatId, 1L) }
        verify { chatRepository.addUserToChat(chatId, 2L) }
    }

    @Test
    fun `getAllChats should return chats from repository`() {
        val chats = listOf(
            Chat(1L, "Chat 1"),
            Chat(2L, "Chat 2")
        )

        every { chatRepository.getAllChats() } returns chats

        val result = chatController.getAllChats()

        assertEquals(chats, result)
        verify { chatRepository.getAllChats() }
    }

    @Test
    fun `addUsers should add users to chat`() {
        val chatId = 1L
        val userIds = listOf(3L, 4L)

        every { chatRepository.addUserToChat(chatId, any()) } returns true

        val result = chatController.addUsers(chatId, userIds)

        assertTrue(result)
        verify { chatRepository.addUserToChat(chatId, 3L) }
        verify { chatRepository.addUserToChat(chatId, 4L) }
    }

    @Test
    fun `getAllChatsEnriched should return formatted chats`() {
        val chats = listOf(
            Chat(1L, "Chat 1"),
            Chat(2L, "Chat 2")
        )

        every { chatRepository.getAllChats() } returns chats

        val result = chatController.getAllChatsEnriched()

        assertTrue(result.containsKey("chats"))
        val items = result["chats"] as List<*>
        assertEquals(2, items.size)
        verify { chatRepository.getAllChats() }
    }
}

