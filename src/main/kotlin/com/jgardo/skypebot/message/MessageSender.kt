package com.jgardo.skypebot.message

import com.jgardo.skypebot.message.model.Message
import io.vertx.core.Vertx
import io.vertx.core.eventbus.MessageProducer
import io.vertx.core.logging.LoggerFactory
import javax.inject.Inject

class MessageSender @Inject constructor(vertx : Vertx) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val messageProducer : MessageProducer<Message> = vertx.eventBus()
            .sender<Message>(MessageBusEvent.SEND.eventName)
            .exceptionHandler({ex->logger.error("Error when sending message", ex)})


    fun send(message:Message) {
        messageProducer.send(message)
    }
}