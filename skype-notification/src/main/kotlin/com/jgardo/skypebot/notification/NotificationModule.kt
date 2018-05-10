package com.jgardo.skypebot.notification

import com.google.inject.Key
import com.google.inject.PrivateModule
import com.jgardo.skypebot.server.BaseRoute
import com.jgardo.skypebot.vertx.VertxConfigurer

class NotificationModule() : PrivateModule() {
    override fun configure() {
        val key = Key.get(NotificationRoute::class.java)

        bind(BaseRoute::class.java).to(key)

        bind(VertxConfigurer::class.java).to(Key.get(NotificationVertxConfigurer::class.java))

    }
}