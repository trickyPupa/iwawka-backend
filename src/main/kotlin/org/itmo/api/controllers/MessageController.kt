package org.itmo.api.controllers

import org.itmo.api.request.SendMessageRequest
import org.itmo.model.Message
import org.itmo.model.User
import org.itmo.service.MessageService

class MessageController(private val messageService: MessageService) {

    fun sendMessage(request: SendMessageRequest, user: User) {
        val message = Message.of(request.text, user.id, request.chatId)

        messageService.sendMessage(message)
    }

    fun deleteMessage(messageId: Long) = messageService.deleteMessage(messageId)

    fun getByChat(chatId: Long): List<Message> {
        return messageService.getByChat(chatId)
    }
}