package org.itmo.service

import org.itmo.db.PostgresClient

class ChatService(private val postgresClient: PostgresClient) {

    fun createChat(chatName: String, memberIds: List<String>): Boolean {
        return true
    }
}