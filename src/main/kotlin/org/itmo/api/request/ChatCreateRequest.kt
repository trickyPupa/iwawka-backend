package org.itmo.api.request

data class ChatCreateRequest(
    val name: String,
    val members: List<Long>,
)
