package com.jgardo.skypebot.message

import com.jgardo.skypebot.server.BaseRoute
import com.jgardo.skypebot.message.model.Message
import io.vertx.core.Vertx
import io.vertx.core.eventbus.MessageProducer
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router

class MessageRoute(private val messageSender: MessageProducer<Message>) : BaseRoute() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun configure(router: Router, vertx: Vertx) {
        router.post("/message/*")
                .configureRestRoutingWithBody()
                .handler { ctx ->
                    val body = ctx.bodyAsJson

                    messageSender.send(body.mapTo(Message::class.java))

                    ctx.response().end("<h1>Sent!</h1>")
                }
    }
}