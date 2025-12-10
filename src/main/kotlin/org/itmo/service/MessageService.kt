package org.itmo.service

import org.itmo.model.Message
import org.itmo.repository.MessageRepository

class MessageService(private val resource: MessageRepository) {

    fun sendMessage(text: String, chat: Long): Boolean{
        return resource.createMessage(text, chat, 1) > 0
    }

    fun deleteMessage(messageId: Long): Boolean {
        return resource.deleteMessage(messageId) > 0
    }

    fun getByChat(chatId: Long): List<Message> {
        return resource.getMessagesByChatId(chatId)
    }
}