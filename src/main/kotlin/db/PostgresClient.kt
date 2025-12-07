package db

import model.Message
import model.User
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

    fun saveMessage(message: Message): Boolean {
        val query = """
            INSERT INTO messages (id, content, sender_id, timestamp)
            VALUES ('${message.id}', '${message.content}', '${message.senderId}', ${message.timestamp})
            ON CONFLICT (id) DO NOTHING
        """.trimIndent()
        return executeUpdate(query) > 0
    }

    fun getMessagesForUser(userId: String): List<Message> {
        val query = "SELECT id, content, sender_id, timestamp FROM messages WHERE sender_id = '$userId'"
        return executeQuery(query) { rs ->
            Message(
                id = rs.getString("id"),
                content = rs.getString("content"),
                senderId = rs.getString("sender_id"),
                timestamp = rs.getLong("timestamp")
            )
        }
    }

    fun deleteMessage(messageId: String): Boolean {
        val query = "DELETE FROM messages WHERE id = '$messageId'"
        return executeUpdate(query) > 0
    }

    fun getAllMessages(): List<Message> {
        val query = "SELECT id, content, sender_id, timestamp FROM messages ORDER BY timestamp DESC"
        return executeQuery(query) { rs ->
            Message(
                id = rs.getString("id"),
                content = rs.getString("content"),
                senderId = rs.getString("sender_id"),
                timestamp = rs.getLong("timestamp")
            )
        }
    }

    fun getAllUsers(): List<User> {
        val query = "SELECT id, username, email FROM users ORDER BY username"
        return executeQuery(query) { rs ->
            User(
                id = rs.getLong("id"),
                username = rs.getString("username"),
                email = rs.getString("email")
            )
        }
    }
}