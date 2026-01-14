package org.itmo.service.user

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import org.slf4j.LoggerFactory

data class RemoteUser(
    val id: Long,
    val username: String?,
    val email: String?,
    val status: Int?,
    val bio: String?,
    val imageId: Long?
)

class UserClient(
    private val baseUrl: String,
    private val apiToken: String? = null
) {
    private val logger = LoggerFactory.getLogger(UserClient::class.java)

    private val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private val http = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }

    suspend fun getUsers(userIds: Set<Long>): Map<Long, RemoteUser> {
        if (userIds.isEmpty()) return emptyMap()
        return try {
            val response = http.post("$baseUrl/api/user/batch") {
                contentType(ContentType.Application.Json)
                apiToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                setBody(mapOf("userIds" to userIds))
            }
            if (!response.status.isSuccess()) {
                logger.warn("Failed to fetch users, status=${response.status}")
                emptyMap()
            } else {
                val payload: Map<String, Any?> = response.body()
                val data = payload["data"]
                val usersJson = mapper.writeValueAsString(data)
                val users: List<RemoteUser> = mapper.readValue(usersJson)
                users.associateBy { it.id }
            }
        } catch (e: Exception) {
            logger.error("Failed to fetch users: ${e.message}", e)
            emptyMap()
        }
    }
}

