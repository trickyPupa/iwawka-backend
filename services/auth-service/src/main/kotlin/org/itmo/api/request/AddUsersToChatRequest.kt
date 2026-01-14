package org.itmo.api.request

data class AddUsersToChatRequest(
    val userIds: List<Long>,
)
