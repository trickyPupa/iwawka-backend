package org.itmo.api.controllers

import io.mockk.*
import kotlin.test.*
import kotlinx.coroutines.runBlocking
import org.itmo.model.Message
import org.itmo.service.AiService
import org.itmo.service.MessageService
import java.time.Instant

class AiControllerTest {

    private lateinit var aiService: AiService
    private lateinit var messageService: MessageService
    private lateinit var aiController: AiController

    @BeforeTest
    fun setup() {
        aiService = mockk()
        messageService = mockk()
        aiController = AiController(aiService, messageService)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `summarizeByInterval should return summary`() = runBlocking {
        val chatId = 1L
        val interval = 60L
        val messages = listOf(
            Message(1L, "Message 1", 1L, chatId, Instant.now()),
            Message(2L, "Message 2", 2L, chatId, Instant.now())
        )
        val summary = "Summary of the conversation"

        every { messageService.getByChatInterval(chatId, interval) } returns messages
        coEvery { aiService.summarizeDialog(any()) } returns summary

        val result = aiController.summarizeByInterval(chatId, interval)

        assertEquals(summary, result)
        verify { messageService.getByChatInterval(chatId, interval) }
        coVerify { aiService.summarizeDialog(any()) }
    }

    @Test
    fun `summarizeByIds should return summary`() = runBlocking {
        val ids = listOf(1L, 2L)
        val messages = listOf(
            Message(1L, "Message 1", 1L, 1L, Instant.now()),
            Message(2L, "Message 2", 2L, 1L, Instant.now())
        )
        val summary = "Summary of selected messages"

        every { messageService.getByIds(ids) } returns messages
        coEvery { aiService.summarizeDialog(any()) } returns summary

        val result = aiController.summarizeByIds(ids)

        assertEquals(summary, result)
        verify { messageService.getByIds(ids) }
        coVerify { aiService.summarizeDialog(any()) }
    }
}

