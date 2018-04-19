package com.jgardo.skypebot.notification

import com.google.inject.AbstractModule
import com.google.inject.TypeLiteral
import com.google.inject.name.Names
import com.jgardo.skypebot.message.model.Message
import com.jgardo.skypebot.util.TextTranslator
import io.vertx.core.eventbus.MessageProducer

class NotificationModule(private val appId : String,
                         private val messageSender: MessageProducer<Message>,
                         private val textTranslator: TextTranslator) : AbstractModule() {
    override fun configure() {
        bind(String::class.java)
                .annotatedWith(Names.named("appId"))
                .toInstance(appId)

        bind(MessageSenderTypeLiteral())
                .annotatedWith(Names.named("messageSender"))
                .toInstance(messageSender)
        bind(TextTranslator::class.java)
                .toInstance(textTranslator)
    }

    private class MessageSenderTypeLiteral : TypeLiteral<MessageProducer<Message>>(){}
}