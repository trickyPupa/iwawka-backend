package org.itmo.service

import kotlinx.coroutines.*
import org.itmo.repository.TokenRepository
import org.slf4j.LoggerFactory
import java.time.Instant
import kotlin.time.Duration.Companion.hours

class TokenCleanupService(
    private val tokenRepository: TokenRepository
) {
    private val logger = LoggerFactory.getLogger(TokenCleanupService::class.java)
    private var cleanupJob: Job? = null

    /**
     * Запускает периодическую очистку истёкших токенов
     * @param intervalHours интервал между очистками в часах (по умолчанию 24 часа)
     */
    fun startCleanup(intervalHours: Long = 24) {
        cleanupJob?.cancel()

        cleanupJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    cleanup()
                    delay(intervalHours.hours)
                } catch (e: CancellationException) {
                    logger.info("Token cleanup service stopped")
                    throw e
                } catch (e: Exception) {
                    logger.error("Error during token cleanup", e)
                    // Продолжаем работу даже при ошибке
                    delay(1.hours) // Попробуем снова через час
                }
            }
        }

        logger.info("Token cleanup service started (interval: $intervalHours hours)")
    }

    /**
     * Останавливает периодическую очистку
     */
    fun stopCleanup() {
        cleanupJob?.cancel()
        cleanupJob = null
        logger.info("Token cleanup service stopped")
    }

    /**
     * Выполняет однократную очистку истёкших токенов
     * @return количество удалённых токенов
     */
    fun cleanup(): Int {
        val now = Instant.now()
        val deletedCount = tokenRepository.deleteExpiredTokens(now)

        if (deletedCount > 0) {
            logger.info("Cleaned up $deletedCount expired tokens")
        }

        return deletedCount
    }
}

