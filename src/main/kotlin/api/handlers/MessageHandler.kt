package api.handlers

import model.Message
import service.MessageService

class MessageHandler(private val messageService: MessageService) {

    fun sendMessage(message: Message) {
        messageService.sendMessage(message)
    }

    fun getAllMessages() = messageService.getAllMessages()

    fun deleteMessage(messageId: String) = messageService.deleteMessage(messageId)

    fun getAllUsers() = messageService.getAllUsers()
}