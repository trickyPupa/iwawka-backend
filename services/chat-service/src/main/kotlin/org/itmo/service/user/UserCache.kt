package org.itmo.service.user

import org.slf4j.LoggerFactory
import org.itmo.db.RedisClient
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

class UserCache(private val redis: RedisClient) {
    private val logger = LoggerFactory.getLogger(UserCache::class.java)
    private val mapper = jacksonObjectMapper()

    fun put(user: RemoteUser, ttlSeconds: Long = 600) {
        val key = key(user.id)
        redis.setex(key, mapper.writeValueAsString(user), ttlSeconds)
    }

    fun get(id: Long): RemoteUser? {
        val value = redis.get(key(id)) ?: return null
        return try {
            mapper.readValue<RemoteUser>(value)
        } catch (e: Exception) {
            logger.warn("Failed to decode cached user $id", e)
            null
        }
    }

    fun getMany(ids: Set<Long>): Map<Long, RemoteUser> {
        if (ids.isEmpty()) return emptyMap()
        val result = mutableMapOf<Long, RemoteUser>()
        ids.forEach { id ->
            get(id)?.let { result[id] = it }
        }
        return result
    }

    private fun key(id: Long) = "user:$id"
}
