package org.itmo.db

import com.clickhouse.jdbc.ClickHouseDataSource
import org.itmo.config.Config
import java.sql.Connection
import java.util.Properties

object ClickHouseConnection {
    private val dataSource: ClickHouseDataSource by lazy {
        val properties = Properties().apply {
            setProperty("user", Config.clickhouseUser)
            setProperty("password", Config.clickhousePassword)
        }
        ClickHouseDataSource(Config.clickhouseUrl, properties)
    }

    fun getConnection(): Connection {
        return dataSource.connection
    }

    /**
     * Инициализация таблиц ClickHouse
     */
    fun initializeTables() {
        getConnection().use { connection ->
            connection.createStatement().use { statement ->
                // Таблица для логов аудита
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS audit_logs (
                        timestamp DateTime64(3),
                        userId Nullable(Int64),
                        username Nullable(String),
                        action String,
                        entityType Nullable(String),
                        entityId Nullable(Int64),
                        details Nullable(String),
                        ipAddress Nullable(String),
                        userAgent Nullable(String),
                        success Bool,
                        errorMessage Nullable(String),
                        date Date DEFAULT toDate(timestamp)
                    ) ENGINE = MergeTree()
                    PARTITION BY toYYYYMM(date)
                    ORDER BY (date, timestamp, action)
                    TTL date + INTERVAL 90 DAY
                    SETTINGS index_granularity = 8192
                """.trimIndent())

                // Таблица для логов HTTP запросов
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS request_logs (
                        timestamp DateTime64(3),
                        method String,
                        uri String,
                        statusCode Int32,
                        duration Int64,
                        userId Nullable(Int64),
                        username Nullable(String),
                        ipAddress Nullable(String),
                        userAgent Nullable(String),
                        requestBody Nullable(String),
                        responseBody Nullable(String),
                        errorMessage Nullable(String),
                        date Date DEFAULT toDate(timestamp)
                    ) ENGINE = MergeTree()
                    PARTITION BY toYYYYMM(date)
                    ORDER BY (date, timestamp, statusCode)
                    TTL date + INTERVAL 30 DAY
                    SETTINGS index_granularity = 8192
                """.trimIndent())

                // Таблица для логов операций с базой данных
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS database_logs (
                        timestamp DateTime64(3),
                        operation String,
                        tableName String,
                        recordId Nullable(Int64),
                        userId Nullable(Int64),
                        username Nullable(String),
                        query Nullable(String),
                        duration Int64,
                        success Bool,
                        errorMessage Nullable(String),
                        affectedRows Nullable(Int32),
                        date Date DEFAULT toDate(timestamp)
                    ) ENGINE = MergeTree()
                    PARTITION BY toYYYYMM(date)
                    ORDER BY (date, timestamp, tableName)
                    TTL date + INTERVAL 60 DAY
                    SETTINGS index_granularity = 8192
                """.trimIndent())

                println("✅ ClickHouse tables initialized successfully")
            }
        }
    }
}

