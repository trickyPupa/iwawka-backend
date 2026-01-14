package org.itmo.model

import java.time.Instant

/**
 * Модель для логов аудита действий пользователей
 */
data class AuditLog(
    val timestamp: Instant,
    val userId: Long?,
    val username: String?,
    val action: String,
    val entityType: String?,
    val entityId: Long?,
    val details: String?,
    val ipAddress: String?,
    val userAgent: String?,
    val success: Boolean,
    val errorMessage: String?
)

/**
 * Модель для логов HTTP запросов
 */
data class RequestLog(
    val timestamp: Instant,
    val method: String,
    val uri: String,
    val statusCode: Int,
    val duration: Long,
    val userId: Long?,
    val username: String?,
    val ipAddress: String?,
    val userAgent: String?,
    val requestBody: String?,
    val responseBody: String?,
    val errorMessage: String?
)

/**
 * Модель для логов операций с базой данных
 */
data class DatabaseLog(
    val timestamp: Instant,
    val operation: String,
    val tableName: String,
    val recordId: Long?,
    val userId: Long?,
    val username: String?,
    val query: String?,
    val duration: Long,
    val success: Boolean,
    val errorMessage: String?,
    val affectedRows: Int?
)

/**
 * Тип действия для аудита
 */
enum class AuditAction(val action: String) {
    // Аутентификация
    LOGIN("USER_LOGIN"),
    LOGOUT("USER_LOGOUT"),
    REGISTER("USER_REGISTER"),
    TOKEN_REFRESH("TOKEN_REFRESH"),
    PASSWORD_CHANGE("PASSWORD_CHANGE"),

    // Операции с пользователями
    USER_CREATE("USER_CREATE"),
    USER_UPDATE("USER_UPDATE"),
    USER_DELETE("USER_DELETE"),
    USER_VIEW("USER_VIEW"),
    USER_LIST("USER_LIST"),

    // Операции с чатами
    CHAT_CREATE("CHAT_CREATE"),
    CHAT_UPDATE("CHAT_UPDATE"),
    CHAT_DELETE("CHAT_DELETE"),
    CHAT_VIEW("CHAT_VIEW"),
    CHAT_LIST("CHAT_LIST"),
    CHAT_JOIN("CHAT_JOIN"),
    CHAT_LEAVE("CHAT_LEAVE"),

    // Операции с сообщениями
    MESSAGE_SEND("MESSAGE_SEND"),
    MESSAGE_UPDATE("MESSAGE_UPDATE"),
    MESSAGE_DELETE("MESSAGE_DELETE"),
    MESSAGE_VIEW("MESSAGE_VIEW"),
    MESSAGE_LIST("MESSAGE_LIST"),

    // Операции с изображениями
    IMAGE_UPLOAD("IMAGE_UPLOAD"),
    IMAGE_DELETE("IMAGE_DELETE"),
    IMAGE_VIEW("IMAGE_VIEW"),

    // Системные операции
    SYSTEM_ERROR("SYSTEM_ERROR"),
    DATABASE_ERROR("DATABASE_ERROR"),
    UNAUTHORIZED_ACCESS("UNAUTHORIZED_ACCESS")
}

/**
 * Тип сущности для аудита
 */
enum class EntityType(val type: String) {
    USER("USER"),
    CHAT("CHAT"),
    MESSAGE("MESSAGE"),
    IMAGE("IMAGE"),
    TOKEN("TOKEN")
}

