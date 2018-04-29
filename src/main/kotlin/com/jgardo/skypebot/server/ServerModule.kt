package com.jgardo.skypebot.server

import com.google.inject.AbstractModule
import com.google.inject.Key
import com.google.inject.Module
import com.google.inject.internal.LinkedBindingImpl
import com.google.inject.multibindings.Multibinder
import com.google.inject.spi.Elements
import com.google.inject.spi.LinkedKeyBinding
import com.google.inject.spi.PrivateElements
import com.jgardo.skypebot.config.BaseConfigProvider
import com.jgardo.skypebot.config.ConfigProvider
import io.vertx.core.Vertx


class ServerModule(private val modules : Set<Module>, private val vertx: Vertx) : AbstractModule() {
    override fun configure() {
        val routeBindings = Multibinder.newSetBinder(binder(), Key.get(BaseRoute::class.java))
        val configBindings = Multibinder.newSetBinder(binder(), Key.get(ConfigProvider::class.java))
        for (module in modules) {
            val elements = Elements.getElements(module).find { it is PrivateElements } as PrivateElements
            val baseRoute = elements.elements
                    .find { it is LinkedKeyBinding<*>
                            && BaseRoute::class.java.isAssignableFrom(it.linkedKey.typeLiteral.rawType) }
                    as LinkedBindingImpl<out BaseRoute>?
            if (baseRoute != null) {
                routeBindings.addBinding().to(baseRoute.linkedKey)
            }

            val configProvider = elements.elements
                    .find { it is LinkedKeyBinding<*>
                            && ConfigProvider::class.java.isAssignableFrom(it.linkedKey.typeLiteral.rawType) }
                    as LinkedBindingImpl<out ConfigProvider>?
            if (configProvider != null) {
                configBindings.addBinding().to(configProvider.linkedKey)
            }
        }
        configBindings.addBinding().to(Key.get(BaseConfigProvider::class.java))

        bind(Vertx::class.java).toInstance(vertx)
        bind(ServerVerticle::class.java)
    }
}
