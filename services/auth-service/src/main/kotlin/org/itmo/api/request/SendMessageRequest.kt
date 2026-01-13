package org.itmo.api.request

data class SendMessageRequest(
    val text: String,
    val chatId: Long
)
