package com.jgardo.skypebot.notification

import com.jgardo.skypebot.notification.model.Activity
import com.jgardo.skypebot.notification.model.Event
import com.jgardo.skypebot.util.ObjectMessageCodec
import com.jgardo.skypebot.vertx.VertxConfigurer
import io.vertx.core.Vertx

class NotificationVertxConfigurer : VertxConfigurer {
    override fun configure(vertx: Vertx) {
        vertx.eventBus().registerDefaultCodec(Activity::class.java, ObjectMessageCodec(Activity::class.java))
    }
}