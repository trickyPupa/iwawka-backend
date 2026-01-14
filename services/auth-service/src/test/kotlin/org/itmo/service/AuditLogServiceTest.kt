package org.itmo.service

import io.mockk.*
import kotlin.test.*
import kotlinx.coroutines.runBlocking
import org.itmo.model.*
import org.itmo.repository.AuditLogRepository
import java.time.Instant

class AuditLogServiceTest {

    private lateinit var auditLogRepository: AuditLogRepository
    private lateinit var auditLogService: AuditLogService

    @BeforeTest
    fun setup() {
        auditLogRepository = mockk(relaxed = true)
        auditLogService = AuditLogService(auditLogRepository)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `logAction should save audit log`() = runBlocking {
        val userId = 1L
        val username = "testuser"
        val action = AuditAction.USER_CREATE
        val entityType = EntityType.USER
        val entityId = 1L

        coEvery { auditLogRepository.saveAuditLog(any()) } returns Unit

        auditLogService.logAction(
            userId = userId,
            username = username,
            action = action,
            entityType = entityType,
            entityId = entityId
        )

        coVerify { auditLogRepository.saveAuditLog(any()) }
    }

    @Test
    fun `logRequest should save request log`() = runBlocking {
        val method = "GET"
        val uri = "/api/test"
        val statusCode = 200
        val duration = 100L

        coEvery { auditLogRepository.saveRequestLog(any()) } returns Unit

        auditLogService.logRequest(
            method = method,
            uri = uri,
            statusCode = statusCode,
            duration = duration
        )

        coVerify { auditLogRepository.saveRequestLog(any()) }
    }

    @Test
    fun `logDatabaseOperation should save database log`() = runBlocking {
        val operation = "INSERT"
        val tableName = "users"
        val recordId = 1L

        coEvery { auditLogRepository.saveDatabaseLog(any()) } returns Unit

        auditLogService.logDatabaseOperation(
            operation = operation,
            tableName = tableName,
            recordId = recordId
        )

        coVerify { auditLogRepository.saveDatabaseLog(any()) }
    }

    @Test
    fun `getAuditLogs should return logs from repository`() = runBlocking {
        val startTime = Instant.now().minusSeconds(3600)
        val endTime = Instant.now()
        val userId = 1L
        val action = "USER_CREATE"
        val limit = 100

        val expectedLogs = listOf(
            AuditLog(
                timestamp = Instant.now(),
                userId = userId,
                username = "testuser",
                action = action,
                entityType = "USER",
                entityId = 1L,
                details = "Test log",
                ipAddress = "127.0.0.1",
                userAgent = "test-agent",
                success = true,
                errorMessage = null
            )
        )

        coEvery {
            auditLogRepository.getAuditLogs(startTime, endTime, userId, action, limit)
        } returns expectedLogs

        val result = auditLogService.getAuditLogs(startTime, endTime, userId, action, limit)

        assertEquals(expectedLogs, result)
        coVerify { auditLogRepository.getAuditLogs(startTime, endTime, userId, action, limit) }
    }

    @Test
    fun `getUserActionStats should return stats from repository`() = runBlocking {
        val userId = 1L
        val days = 7
        val expectedStats = mapOf(
            "USER_CREATE" to 5L,
            "USER_UPDATE" to 3L
        )

        coEvery { auditLogRepository.getUserActionStats(userId, days) } returns expectedStats

        val result = auditLogService.getUserActionStats(userId, days)

        assertEquals(expectedStats, result)
        coVerify { auditLogRepository.getUserActionStats(userId, days) }
    }
}

