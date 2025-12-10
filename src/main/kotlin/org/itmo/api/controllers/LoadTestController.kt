package org.itmo.api.controllers

import org.itmo.api.logger
import org.itmo.service.MessageService

class LoadTestController(private val messageService: MessageService) {
    fun test(): Boolean {
        try{
            messageService.getByChat(1)
            return true
        } catch (e: Exception) {
            logger.error(e.message, e)
            return false
        }
    }
}