package com.jgardo.skypebot

import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.Injector
import com.jgardo.skypebot.authentication.AuthenticationVerticle
import com.jgardo.skypebot.message.MessageBusEvent
import com.jgardo.skypebot.message.MessageVerticle
import com.jgardo.skypebot.notification.NotificationController
import com.jgardo.skypebot.notification.NotificationModule
import io.vertx.core.Vertx
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

import io.vertx.core.logging.LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.SLF4JLogDelegateFactory
import io.vertx.core.logging.LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME



class SkypebotApplication : AbstractVerticle() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun start(fut: Future<Void>) {
        vertx
                .createHttpServer()
                .requestHandler(routes(vertx)::accept)
                .listen(Integer.getInteger("server.port",8080)) { result ->
                    if (result.succeeded()) {
                        logger.info("Http listener startup completed")
                        fut.complete()
                    } else {
                        logger.error("Http listener startup failed", result.cause())
                        fut.fail(result.cause())
                    }
                }
    }

    private fun routes(vertx:Vertx) : Router {
        val route = Router.router(vertx)

        route.route("/*").handler(BodyHandler.create())

        val injector = Guice.createInjector(NotificationModule())

        route.route("/notification/*").handler { ctx ->
            val body = ctx.bodyAsJson
            val controller = injector.getInstance(NotificationController::class.java)

            controller.notify(body)
            ctx.response().end("<h1>Notified!</h1>")
        }

        route.post("/message/*").handler { ctx ->
            val body = ctx.bodyAsJson

            vertx.eventBus().send(MessageBusEvent.SEND.eventName, Json.encode(body))

            ctx.response().end("<h1>Sent!</h1>")
        }

        return route
    }
}

fun main(args : Array<String>) {
    System.setProperty(LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory::class.java.name)

    val vertx = Vertx.vertx()

    val logger = LoggerFactory.getLogger(SkypebotApplication::class.java)

    fun logStarted(name : String, ar:AsyncResult<String>) {
        if (ar.succeeded()) {
            logger.info("Verticle $name started.")
        } else {
            logger.error("Problem occurs when verticle $name starts.", ar.cause())
        }
    }

    vertx.deployVerticle(MessageVerticle(), {ar -> logStarted("MessageVerticle", ar)})
    vertx.deployVerticle(SkypebotApplication(), {ar -> logStarted("SkypebotApplication", ar)})
    vertx.deployVerticle(AuthenticationVerticle(), {ar -> logStarted("AuthenticationVerticle", ar)})
}
