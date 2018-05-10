package com.jgardo.skypebot.vertx

import io.vertx.core.Vertx
import javax.inject.Inject

class VertxConfigurationService @Inject constructor(
        private val configurers : java.util.Set<VertxConfigurer>,
        private val vertx : Vertx) {

    fun configure() {
        for (configurer in configurers) {
            configurer.configure(vertx)
        }
    }
}