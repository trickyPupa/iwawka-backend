package org.itmo.api.controllers

import org.itmo.service.AiService
import org.itmo.service.MessageService

class AiController(private val aiService: AiService, private val messageService: MessageService) {
    suspend fun summarizeByInterval(chatId: Long, interval: Long): String {
        val messages = messageService.getByChatInterval(chatId, interval)

        val dialogMessages = messages.map { message ->
            org.itmo.service.DialogMessage(
                senderId = message.senderId,
                timestamp = message.createdAt.toEpochMilli(),
                text = message.content
            )
        }

        val result = aiService.summarizeDialog(dialogMessages)

        return result
    }

    suspend fun summarizeByIds(ids: List<Long>): String {
        val messages = messageService.getByIds(ids)

        val dialogMessages = messages.map { message ->
            org.itmo.service.DialogMessage(
                senderId = message.senderId,
                timestamp = message.createdAt.toEpochMilli(),
                text = message.content
            )
        }

        val result = aiService.summarizeDialog(dialogMessages)

        return result
    }
}