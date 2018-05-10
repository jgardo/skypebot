package com.jgardo.skypebot.message

import com.google.inject.Key
import com.google.inject.PrivateModule
import com.jgardo.skypebot.server.BaseRoute
import com.jgardo.skypebot.vertx.VertxConfigurer


class DirectMessageModule() : PrivateModule() {
    override fun configure() {
        val key = Key.get(DirectMessageRoute::class.java)
        bind(BaseRoute::class.java).to(key)

        bind(VertxConfigurer::class.java).to(Key.get(DirectMessageVertxConfigurer::class.java))
    }
}