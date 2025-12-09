package org.itmo.service

import org.itmo.model.Message
import org.itmo.resources.MessageResource

class MessageService(private val resource: MessageResource) {

    fun sendMessage(message: Message): Boolean{
        return resource.createMessage(message) > 0
    }

    fun deleteMessage(messageId: Long): Boolean {
        return resource.deleteMessage(messageId) > 0
    }

    fun getByChat(chatId: Long): List<Message> {
        return resource.getMessagesByChatId(chatId)
    }
}