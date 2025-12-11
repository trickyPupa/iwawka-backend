package org.itmo.service

import org.itmo.model.Message
import org.itmo.repository.MessageRepository

class MessageService(private val resource: MessageRepository) {

    fun sendMessage(text: String, chat: Long, userId: Long): Boolean{
        return resource.createMessage(text, chat, userId) > 0
    }

    fun deleteMessage(messageId: Long): Boolean {
        return resource.deleteMessage(messageId) > 0
    }

    fun getByChat(chatId: Long): List<Message> {
        return resource.getMessagesByChatId(chatId)
    }

    fun getByChatInterval(chatId: Long, interval: Long): List<Message> {
        return resource.getMessagesByChatInterval(chatId, interval)
    }

    fun getByIds(ids: List<Long>): List<Message> {
        return resource.getMessagesByIds(ids)
    }
}