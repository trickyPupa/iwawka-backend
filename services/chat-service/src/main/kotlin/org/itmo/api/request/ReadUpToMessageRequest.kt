package org.itmo.api.request

data class MessageReadUpToRequest(
    val chatId: Long,
    val messageId: Long
)