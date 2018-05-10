package com.jgardo.skypebot.message

import com.google.inject.Key
import com.google.inject.PrivateModule
import com.jgardo.skypebot.server.BaseRoute


class DirectMessageModule() : PrivateModule() {
    override fun configure() {
        val key = Key.get(DirectMessageRoute::class.java)
        bind(BaseRoute::class.java).to(key)
    }
}