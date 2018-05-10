package com.jgardo.skypebot.message

import com.jgardo.skypebot.message.model.Message
import com.jgardo.skypebot.server.BaseRoute
import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import javax.inject.Inject

class DirectMessageRoute @Inject constructor(private val messageSender: MessageSender) : BaseRoute() {

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