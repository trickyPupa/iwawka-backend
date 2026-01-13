package org.itmo.db

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

class ClickHouseClient(private val url: String, private val user: String, private val password: String) {

    private var connection: Connection? = null

    init {
        connect()
    }

    private fun connect() {
        connection = DriverManager.getConnection(url, user, password)
    }

    fun executeQuery(query: String): ResultSet? {
        val statement: Statement? = connection?.createStatement()
        return statement?.executeQuery(query)
    }

    fun executeUpdate(query: String): Int {
        val statement: Statement? = connection?.createStatement()
        return statement?.executeUpdate(query) ?: 0
    }

    fun close() {
        connection?.close()
    }
}