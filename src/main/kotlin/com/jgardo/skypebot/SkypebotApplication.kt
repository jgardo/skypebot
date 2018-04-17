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
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

class SkypebotApplication : AbstractVerticle() {
    override fun start(fut: Future<Void>) {
        vertx
                .createHttpServer()
                .requestHandler(routes(vertx)::accept)
                .listen(Integer.getInteger("server.port",8080)) { result ->
                    if (result.succeeded()) {
                        fut.complete()
                    } else {
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
    val vertx = Vertx.vertx()
    vertx.deployVerticle(MessageVerticle())
    vertx.deployVerticle(SkypebotApplication())
    vertx.deployVerticle(AuthenticationVerticle())

}
