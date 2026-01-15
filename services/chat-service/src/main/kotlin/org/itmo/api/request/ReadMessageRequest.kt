package org.itmo.api.request

data class MessageReadRequest(
    val chatId: Long,
    val messageIds: List<Long>
)