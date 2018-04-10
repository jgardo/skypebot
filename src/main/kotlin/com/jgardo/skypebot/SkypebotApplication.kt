package com.jgardo.skypebot

import io.vertx.core.Vertx

fun main(args : Array<String>) {
    val vertx = Vertx.vertx()
    val server = vertx.createHttpServer()

    server.requestHandler({ req ->
        req.response().end("Hello world")
    })

    server.listen(8080)
}
