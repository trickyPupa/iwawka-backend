package org.itmo.model

import java.time.LocalDateTime

data class Message(
    val id: Long? = null,
    val content: String,
    val senderId: Long,
    val chatId: Long,
    val created: LocalDateTime?,
    val deleted: LocalDateTime? = null,
) {
    companion object {
        fun of(content: String, user: Long, chat: Long): Message {
            return Message(
                content = content,
                senderId = user,
                chatId = chat,
                created = LocalDateTime.now(),
            )
        }
    }
}