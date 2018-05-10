package com.jgardo.skypebot.greeting

import com.google.inject.Key
import com.google.inject.PrivateModule
import com.jgardo.skypebot.vertx.VertxConfigurer

class GreetingModule() : PrivateModule() {
    override fun configure() {
        bind(VertxConfigurer::class.java).to(Key.get(NotificationListener::class.java))
    }
}