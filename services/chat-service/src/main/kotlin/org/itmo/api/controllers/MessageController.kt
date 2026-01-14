package org.itmo.api.controllers

import kotlinx.coroutines.runBlocking
import org.itmo.api.request.SendMessageRequest
import org.itmo.model.Message
import org.itmo.service.MessageService
import org.itmo.service.user.UserService

class MessageController(
    private val messageService: MessageService,
    private val userService: UserService
) {

    fun sendMessage(request: SendMessageRequest, userId: Long): Boolean {
        return messageService.sendMessage(request.text, request.chatId, userId)
    }

    fun deleteMessage(messageId: Long) = messageService.deleteMessage(messageId)

    fun getByChat(chatId: Long): List<Message> {
        return messageService.getByChat(chatId)
    }

    fun getByChatEnriched(chatId: Long): Map<String, Any> {
        val messages = messageService.getByChat(chatId)
        val userIds = messages.map { it.senderId }.toSet()
        val users = runBlocking { userService.getUsers(userIds) }
        val items = messages.map { msg ->
            mapOf(
                "id" to msg.id,
                "content" to msg.content,
                "senderId" to msg.senderId,
                "sender" to users[msg.senderId],
                "chatId" to msg.chatId,
                "createdAt" to msg.createdAt
            )
        }
        return mapOf("messages" to items)
    }

    fun markAsRead(chatId: Long, messageIds: List<Long>, userId: Long) {
        messageService.markAsRead(chatId, messageIds, userId)
    }

    fun getNewMessages(chatId: Long, userId: Long): List<Message> {
        return messageService.getNewMessages(chatId, userId)
    }

    fun markAsReadUpTo(chatId: Long, messageId: Long, userId: Long) {
        messageService.markAsReadUpTo(chatId, messageId, userId)
    }
}