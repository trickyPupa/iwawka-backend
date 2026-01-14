package org.itmo.db

import org.postgresql.ds.PGSimpleDataSource
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

class PostgresClient(private val url: String, private val user: String, private val password: String) {
    private val dataSource: PGSimpleDataSource = PGSimpleDataSource().apply {
        setURL(this@PostgresClient.url)
        setUser(this@PostgresClient.user)
        setPassword(this@PostgresClient.password)
    }

    fun getConnection(): Connection {
        return dataSource.connection
    }

    fun <T> executeQuery(query: String, mapper: (ResultSet) -> T): List<T> {
        val connection = getConnection()
        return try {
            connection.createStatement().use { statement ->
                statement.executeQuery(query).use { resultSet ->
                    generateSequence { if (resultSet.next()) mapper(resultSet) else null }.toList()
                }
            }
        } catch (e: SQLException) {
            throw RuntimeException("Error executing query: $query", e)
        } finally {
            connection.close()
        }
    }

    fun executeUpdate(query: String): Int {
        val connection = getConnection()
        return try {
            connection.createStatement().use { statement ->
                statement.executeUpdate(query)
            }
        } catch (e: SQLException) {
            throw RuntimeException("Error executing update: $query", e)
        } finally {
            connection.close()
        }
    }
}