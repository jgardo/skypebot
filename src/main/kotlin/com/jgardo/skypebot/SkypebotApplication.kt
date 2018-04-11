package com.jgardo.skypebot

import io.vertx.core.Vertx
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.ext.web.Router

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

        route.post("/conversation/:conversationId/message").handler { ctx ->
            val body = ctx.bodyAsString
            ctx.response().end("<h1>Hello from my first " + "Vert.x 3 application</h1>")
        }

        route.get("/").handler { ctx ->
            val body = ctx.bodyAsString
            ctx.response().end("<h1>Hello from my first " + "Vert.x 3 application</h1>")
        }

        return route
    }
}

fun main(args : Array<String>) {
    val vertx = Vertx.vertx()
    vertx.deployVerticle(SkypebotApplication())
}
