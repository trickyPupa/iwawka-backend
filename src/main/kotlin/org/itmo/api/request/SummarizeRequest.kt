package org.itmo.api.request

data class SummarizeRequest(
    val chatId: Long,
    val messageIds: List<Long>? = null,
    val interval: Long? = null
)
