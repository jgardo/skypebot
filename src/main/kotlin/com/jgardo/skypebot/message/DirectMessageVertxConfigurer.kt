package com.jgardo.skypebot.message

import com.jgardo.skypebot.message.model.Message
import com.jgardo.skypebot.util.ObjectMessageCodec
import com.jgardo.skypebot.vertx.VertxConfigurer
import io.vertx.core.Vertx

class DirectMessageVertxConfigurer : VertxConfigurer {
    override fun configure(vertx: Vertx) {
        vertx.eventBus().registerDefaultCodec(Message::class.java, ObjectMessageCodec(Message::class.java))
    }
}