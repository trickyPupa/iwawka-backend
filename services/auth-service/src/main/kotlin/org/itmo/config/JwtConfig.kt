package org.itmo.config

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

object JwtConfig {
    private val algorithm = Algorithm.HMAC256(Config.authJwtSecret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(Config.authJwtIssuer)
        .withAudience(Config.authJwtAudience)
        .build()

    fun generateAccessToken(userId: Long): String {
        val now = Instant.now()
        val expiresAt = now.plus(Config.authJwtAccessTtlMinutes, ChronoUnit.MINUTES)

        return JWT.create()
            .withIssuer(Config.authJwtIssuer)
            .withAudience(Config.authJwtAudience)
            .withSubject(userId.toString())
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(expiresAt))
            .withClaim("type", "access")
            .sign(algorithm)
    }

    fun validateToken(token: String): Long? {
        return try {
            val decodedJWT = verifier.verify(token)
            val type = decodedJWT.getClaim("type").asString()
            if (type != "access") {
                null
            } else {
                decodedJWT.subject.toLongOrNull()
            }
        } catch (e: Exception) {
            null
        }
    }

    fun extractUserId(token: String): Long? {
        return try {
            val decodedJWT = JWT.decode(token)
            decodedJWT.subject.toLongOrNull()
        } catch (e: Exception) {
            null
        }
    }
}

