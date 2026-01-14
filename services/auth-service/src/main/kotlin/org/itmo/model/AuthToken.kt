package org.itmo.model

import java.time.Instant

data class AuthToken(
    val id: Long,
    val userId: Long,
    val token: String,
    val tokenType: String,
    val expiresAt: Instant,
    val createdAt: Instant,
    val revoked: Boolean,
)

