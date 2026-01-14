package org.itmo.api.request

data class UserUpdateRequest (
    val name: String? = null,
    val email: String? = null,
    val bio: String? = null,
    val status: Int? = null
)