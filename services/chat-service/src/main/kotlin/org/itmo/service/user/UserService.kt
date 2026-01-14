package org.itmo.service.user

import org.slf4j.LoggerFactory

class UserService(
    private val userClient: UserClient,
    private val userCache: UserCache
) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)

    suspend fun getUsers(ids: Set<Long>): Map<Long, RemoteUser> {
        if (ids.isEmpty()) return emptyMap()

        val cached = userCache.getMany(ids)
        val missing = ids - cached.keys
        if (missing.isEmpty()) return cached

        val fetched = userClient.getUsers(missing)
        fetched.values.forEach { userCache.put(it) }

        return cached + fetched
    }
}

