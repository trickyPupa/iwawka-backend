package service

import db.PostgresClient
import model.Message
import model.User

class MessageService(private val postgresClient: PostgresClient) {

    fun sendMessage(message: Message): Boolean = postgresClient.saveMessage(message)

    fun getMessagesForUser(userId: String): List<Message> = postgresClient.getMessagesForUser(userId)

    fun getAllMessages(): List<Message> = postgresClient.getAllMessages()

    fun deleteMessage(messageId: String): Boolean = postgresClient.deleteMessage(messageId)

    fun getAllUsers(): List<User> = postgresClient.getAllUsers()
}