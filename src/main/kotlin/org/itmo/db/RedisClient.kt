package org.itmo.db

import redis.clients.jedis.JedisPool

class RedisClient(private val host: String, private val port: Int) {
    private val jedisPool: JedisPool = JedisPool(host, port)

    fun get(key: String): String? {
        jedisPool.resource.use { jedis ->
            return jedis.get(key)
        }
    }

    fun set(key: String, value: String) {
        jedisPool.resource.use { jedis ->
            jedis.set(key, value)
        }
    }

    fun delete(key: String) {
        jedisPool.resource.use { jedis ->
            jedis.del(key)
        }
    }

    fun close() {
        jedisPool.close()
    }
}