package org.itmo.config

import com.typesafe.config.ConfigFactory

object Config {
    private val config = ConfigFactory.load()

    private fun string(path: String) = config.getString(path)
    private fun int(path: String) = config.getInt(path)
    private fun long(path: String) = config.getLong(path)

    val serverHost: String = string("server.host")
    val serverPort: Int = int("server.port")

    val postgresUrl: String = string("database.postgres.url")
    val postgresUser: String = string("database.postgres.user")
    val postgresPassword: String = string("database.postgres.password")

    val redisHost: String = string("database.redis.host")
    val redisPort: Int = int("database.redis.port")

    val clickhouseUrl: String = string("database.clickhouse.url")
    val clickhouseUser: String = string("database.clickhouse.user")
    val clickhousePassword: String = string("database.clickhouse.password")

    val flywayEnabled: Boolean = config.getBoolean("flyway.enabled")
    val flywayLocations: List<String> = config.getStringList("flyway.locations")

    val authJwtIssuer: String = string("auth.jwt.issuer")
    val authJwtAudience: String = string("auth.jwt.audience")
    val authJwtSecret: String = string("auth.jwt.secret")
    val authJwtAccessTtlMinutes: Long = long("auth.jwt.accessTtlMinutes")
    val authJwtRefreshTtlDays: Long = long("auth.jwt.refreshTtlDays")

    val gigachatAuthKey: String = string("gigachat.authkey")
    val gigachatModel: String = string("gigachat.model")
}