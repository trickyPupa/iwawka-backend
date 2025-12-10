package org.itmo.service

import org.itmo.config.Config
import org.itmo.config.JwtConfig
import org.itmo.repository.TokenRepository
import org.itmo.repository.UserRepository
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class AuthService(
    private val userRepository: UserRepository,
    private val tokenRepository: TokenRepository
) {
    fun register(username: String, email: String, password: String): Long {
        if (userRepository.getUserByEmail(email) != null) {
            throw IllegalArgumentException("User with this email already exists")
        }

        if (!isValidEmail(email)) {
            throw IllegalArgumentException("Invalid email format")
        }

        if (password.length < 8) {
            throw IllegalArgumentException("Password must be at least 8 characters long")
        }

        val hash = hashPassword(password)

        val userId = userRepository.createUser(username, email, hash)
        userRepository.updateLastLogin(userId, Instant.now())
        return userId
    }

    fun login(email: String, password: String): AuthTokens {
        val user = userRepository.getUserByEmail(email)
            ?: throw IllegalArgumentException("Invalid credentials")

        if (!verifyPassword(password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid credentials")
        }

        userRepository.updateLastLogin(user.id, Instant.now())
        return issueTokens(user.id)
    }

    fun issueTokens(userId: Long): AuthTokens {
        val accessToken = JwtConfig.generateAccessToken(userId)

        val refreshToken = UUID.randomUUID().toString()
        val refreshExpires = Instant.now().plus(Config.authJwtRefreshTtlDays, ChronoUnit.DAYS)

        tokenRepository.saveToken(userId, refreshToken, "refresh", refreshExpires)

        return AuthTokens(accessToken, refreshToken, Config.authJwtAccessTtlMinutes * 60)
    }

    fun refresh(refreshToken: String): AuthTokens {
        val token = tokenRepository.findToken(refreshToken, "refresh")
            ?: throw IllegalArgumentException("Invalid refresh token")

        if (token.revoked) {
            throw IllegalArgumentException("Refresh token has been revoked")
        }

        if (token.expiresAt.isBefore(Instant.now())) {
            throw IllegalArgumentException("Refresh token has expired")
        }

        tokenRepository.revokeToken(refreshToken)

        return issueTokens(token.userId)
    }

    fun revoke(refreshToken: String) {
        tokenRepository.findToken(refreshToken, "refresh")
            ?: throw IllegalArgumentException("Invalid refresh token")
        tokenRepository.revokeToken(refreshToken)
    }

    fun revokeAllUserTokens(userId: Long): Int {
        return tokenRepository.revokeAllUserTokens(userId)
    }

    fun validateAccessToken(token: String): Long? {
        return JwtConfig.validateToken(token)
    }

    private fun hashPassword(password: String): String {
        val salt = BCrypt.gensalt()
        return BCrypt.hashpw(password, salt)
    }

    private fun verifyPassword(password: String, hash: String): Boolean {
        return BCrypt.checkpw(password, hash)
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }
}

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

