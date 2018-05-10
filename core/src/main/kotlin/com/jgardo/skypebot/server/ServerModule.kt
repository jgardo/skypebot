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
import com.jgardo.skypebot.message.MessageVertxConfigurer
import com.jgardo.skypebot.vertx.VertxConfigurer
import io.vertx.core.Vertx


class ServerModule(private val modules : Set<Module>, private val vertx: Vertx) : AbstractModule() {
    override fun configure() {
        val routeBindings = Multibinder.newSetBinder(binder(), Key.get(BaseRoute::class.java))
        val configBindings = Multibinder.newSetBinder(binder(), Key.get(ConfigProvider::class.java))
        val vertxConfigurerBindings = Multibinder.newSetBinder(binder(), Key.get(VertxConfigurer::class.java))
        for (module in modules) {
            val elements = Elements.getElements(module).find { it is PrivateElements } as PrivateElements
            registerClassInModule(elements, routeBindings, BaseRoute::class.java)
            registerClassInModule(elements, configBindings, ConfigProvider::class.java)
            registerClassInModule(elements, vertxConfigurerBindings, VertxConfigurer::class.java)
        }
        configBindings.addBinding().to(Key.get(BaseConfigProvider::class.java))

        bind(Vertx::class.java).toInstance(vertx)
        bind(ServerVerticle::class.java)

        vertxConfigurerBindings .addBinding().to(Key.get(MessageVertxConfigurer::class.java))
    }

    private fun <T> registerClassInModule(elements: PrivateElements, multiBindings: Multibinder<T>, clazz : Class<T> ) {
        val element = elements.elements
                .find {
                    it is LinkedKeyBinding<*>
                            && clazz.isAssignableFrom(it.linkedKey.typeLiteral.rawType)
                }
                as LinkedBindingImpl<out T>?
        if (element != null) {
            multiBindings.addBinding().to(element.linkedKey)
        }
    }
}
