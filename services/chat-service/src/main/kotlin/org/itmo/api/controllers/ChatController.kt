package org.itmo.api.controllers

import org.itmo.model.Chat
import org.itmo.repository.ChatRepository

class ChatController(private val chatRepository: ChatRepository) {

    fun createChat(chatName: String, memberIds: List<Long>): Long {
        val chatId = chatRepository.createChat(chatName)
        var success = true
        for (memberId in memberIds) {
            success = success && chatRepository.addUserToChat(chatId, memberId)
        }
        return chatId
    }

    fun getAllChats(): List<Chat> {
        return chatRepository.getAllChats()
    }

    fun addUsers(chatId: Long, userIds: List<Long>): Boolean {
        var success = true
        for (memberId in userIds) {
            success = success && chatRepository.addUserToChat(chatId, memberId)
        }
        return success
    }
}