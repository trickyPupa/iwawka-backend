package org.itmo.api.controllers

import org.itmo.model.Chat
import org.itmo.repository.ChatRepository
import org.itmo.service.user.UserService

class ChatController(
    private val chatRepository: ChatRepository,
    private val userService: UserService
) {

    fun createChat(chatName: String, memberIds: List<Long>): Long {
        val chatId = chatRepository.createChat(chatName)
        memberIds.forEach { chatRepository.addUserToChat(chatId, it) }
        return chatId
    }

    fun getAllChats(): List<Chat> {
        return chatRepository.getAllChats()
    }

    fun addUsers(chatId: Long, userIds: List<Long>): Boolean {
        userIds.forEach { chatRepository.addUserToChat(chatId, it) }
        return true
    }

    fun getAllChatsEnriched(): Map<String, Any> {
        val chats = chatRepository.getAllChats()
        val items = chats.map { chat ->
            mapOf(
                "id" to chat.id,
                "name" to chat.name
            )
        }
        return mapOf("chats" to items)
    }
}