package org.itmo.model

import java.time.Instant

data class Message(
    val id: Long? = null,
    val content: String,
    val senderId: Long,
    val chatId: Long,
    val createdAt: Instant?,
)