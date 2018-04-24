package com.jgardo.skypebot.server

import com.google.inject.AbstractModule
import com.google.inject.Key
import com.google.inject.Module
import com.google.inject.internal.LinkedBindingImpl
import com.google.inject.multibindings.Multibinder
import com.google.inject.spi.Elements
import com.google.inject.spi.LinkedKeyBinding
import com.google.inject.spi.PrivateElements
import com.jgardo.skypebot.message.DirectMessageRoute
import com.jgardo.skypebot.notification.NotificationRoute
import io.vertx.core.Vertx


class ServerModule(private val modules : Set<Module>, private val vertx: Vertx) : AbstractModule() {
    override fun configure() {
        val key = Key.get(BaseRoute::class.java)
        val multibinder = Multibinder.newSetBinder(binder(), key)
        for (module in modules) {
            val elements = Elements.getElements(module).find { it is PrivateElements } as PrivateElements
            val element = elements.elements
                    .find { it is LinkedKeyBinding<*> } as LinkedBindingImpl<out BaseRoute>
            multibinder.addBinding().to(element.linkedKey)
        }

        multibinder.addBinding().to(NotificationRoute::class.java)
        multibinder.addBinding().to(DirectMessageRoute::class.java)

        bind(Vertx::class.java).toInstance(vertx)

        bind(ServerVerticle::class.java)
    }
}
