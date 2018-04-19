package com.jgardo.skypebot.server

import io.vertx.core.Vertx
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

abstract class BaseRoute {

    private val APPLICATION_JSON = "application/json"

    private val logger = LoggerFactory.getLogger(javaClass::class.java)

    abstract fun configure(router: Router, vertx: Vertx)

    protected fun Route.configureRestRoutingWithBody() : Route {
        return configureRestRouting(true)
    }

    protected fun Route.configureRestRoutingWithoutBody() : Route {
        return configureRestRouting(false)
    }

    private fun Route.configureRestRouting(withBody : Boolean) : Route {
        if (withBody) {
            this.handler(BodyHandler.create())
                    .consumes(APPLICATION_JSON)
        }
        this
                .produces(APPLICATION_JSON)
                .failureHandler({ctx ->
                    logger.error("Handling ${ctx.currentRoute().path} finished with exception", ctx.failure())

                    val result = JsonObject()
                            .put("error", ctx.failure().message)
                    ctx.response().statusCode = 500
                    ctx.response().putHeader(HttpHeaders.CONTENT_TYPE,APPLICATION_JSON)
                    ctx.response().end(result.encode())
                })
        return this
    }
}