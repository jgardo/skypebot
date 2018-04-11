package com.jgardo.skypebot

import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.Injector
import com.jgardo.skypebot.notification.NotificationController
import com.jgardo.skypebot.notification.NotificationModule
import io.vertx.core.Vertx
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
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

        route.post("/conversation/:conversationId/message").handler { ctx ->
            val body = ctx.bodyAsString
            ctx.response().end("<h1>Hello from my first " + "Vert.x 3 application</h1>")
        }

        route.post("/notification/").handler { ctx ->
            val body = ctx.bodyAsString
            val controller = injector.getInstance(NotificationController::class.java)

            controller.notify(body)
            ctx.response().end("<h1>Notified!</h1>")
        }

        return route
    }
}

fun main(args : Array<String>) {
    val vertx = Vertx.vertx()
    vertx.deployVerticle(SkypebotApplication())
}
