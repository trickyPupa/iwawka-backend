package org.itmo.model

import java.time.Instant

data class User(
    val id: Long,
    val username: String,
    val email: String,
    val status: Int = 0,
    val bio: String? = null,
    val imageId: Long? = null,
    val createdAt: Instant,
)