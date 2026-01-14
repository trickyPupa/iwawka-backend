package org.itmo.api.controllers

import io.mockk.*
import kotlin.test.*
import org.itmo.service.MessageService

class LoadTestControllerTest {

    private lateinit var messageService: MessageService
    private lateinit var loadTestController: LoadTestController

    @BeforeTest
    fun setup() {
        messageService = mockk()
        loadTestController = LoadTestController(messageService)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test should return true when successful`() {
        every { messageService.getByChat(1L) } returns emptyList()

        val result = loadTestController.test()

        assertTrue(result)
        verify { messageService.getByChat(1L) }
    }

    @Test
    fun `test should return false when exception occurs`() {
        every { messageService.getByChat(1L) } throws RuntimeException("Database error")

        val result = loadTestController.test()

        assertFalse(result)
        verify { messageService.getByChat(1L) }
    }
}

