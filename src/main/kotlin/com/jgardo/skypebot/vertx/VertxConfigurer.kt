package com.jgardo.skypebot.vertx

import io.vertx.core.Vertx

interface VertxConfigurer {
    fun configure(vertx: Vertx)
}