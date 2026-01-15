package org.itmo.api.controllers

import org.itmo.repository.ChatRepository
import org.itmo.service.AiService
import org.itmo.service.MessageService
import org.slf4j.LoggerFactory

class AiController(private val aiService: AiService, private val messageService: MessageService) {

    val logger = LoggerFactory.getLogger(AiController::class.java)

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

    suspend fun summarizeByMsgs(json: String): String {
        logger.info("Sending to ai service")
        val result = aiService.summarizeJson(json)

        return result;
    }
}