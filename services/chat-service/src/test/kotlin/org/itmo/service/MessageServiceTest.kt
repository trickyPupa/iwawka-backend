package org.itmo.service

import io.mockk.*
import kotlin.test.*
import org.itmo.model.Message
import org.itmo.repository.MessageRepository
import java.time.Instant

class MessageServiceTest {

    private lateinit var messageRepository: MessageRepository
    private lateinit var messageService: MessageService

    @BeforeTest
    fun setup() {
        messageRepository = mockk()
        messageService = MessageService(messageRepository)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `sendMessage should return true when message created`() {
        val text = "Hello"
        val chatId = 1L
        val userId = 1L

        every { messageRepository.createMessage(text, chatId, userId) } returns 1

        val result = messageService.sendMessage(text, chatId, userId)

        assertTrue(result)
        verify { messageRepository.createMessage(text, chatId, userId) }
    }

    @Test
    fun `sendMessage should return false when message not created`() {
        val text = "Hello"
        val chatId = 1L
        val userId = 1L

        every { messageRepository.createMessage(text, chatId, userId) } returns 0

        val result = messageService.sendMessage(text, chatId, userId)

        assertFalse(result)
    }

    @Test
    fun `deleteMessage should return true when message deleted`() {
        val messageId = 1L

        every { messageRepository.deleteMessage(messageId) } returns 1

        val result = messageService.deleteMessage(messageId)

        assertTrue(result)
        verify { messageRepository.deleteMessage(messageId) }
    }

    @Test
    fun `deleteMessage should return false when message not deleted`() {
        val messageId = 1L

        every { messageRepository.deleteMessage(messageId) } returns 0

        val result = messageService.deleteMessage(messageId)

        assertFalse(result)
    }

    @Test
    fun `getByChat should return messages from repository`() {
        val chatId = 1L
        val messages = listOf(
            Message(1L, "Message 1", 1L, chatId, Instant.now()),
            Message(2L, "Message 2", 2L, chatId, Instant.now())
        )

        every { messageRepository.getMessagesByChatId(chatId) } returns messages

        val result = messageService.getByChat(chatId)

        assertEquals(messages, result)
        verify { messageRepository.getMessagesByChatId(chatId) }
    }

    @Test
    fun `getByChatInterval should return messages from repository`() {
        val chatId = 1L
        val interval = 60L
        val messages = listOf(
            Message(1L, "Message 1", 1L, chatId, Instant.now())
        )

        every { messageRepository.getMessagesByChatInterval(chatId, interval) } returns messages

        val result = messageService.getByChatInterval(chatId, interval)

        assertEquals(messages, result)
        verify { messageRepository.getMessagesByChatInterval(chatId, interval) }
    }

    @Test
    fun `getByIds should return messages from repository`() {
        val ids = listOf(1L, 2L)
        val messages = listOf(
            Message(1L, "Message 1", 1L, 1L, Instant.now()),
            Message(2L, "Message 2", 2L, 1L, Instant.now())
        )

        every { messageRepository.getMessagesByIds(ids) } returns messages

        val result = messageService.getByIds(ids)

        assertEquals(messages, result)
        verify { messageRepository.getMessagesByIds(ids) }
    }
}

