package org.itmo.api.response

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

