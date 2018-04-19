package com.jgardo.skypebot.notification

import com.jgardo.skypebot.server.BaseRoute
import com.jgardo.skypebot.message.model.Message
import com.jgardo.skypebot.notification.model.Activity
import io.vertx.core.Vertx
import io.vertx.core.eventbus.MessageProducer
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router

class NotificationRoute(private val appId : String, private val messageSender: MessageProducer<Message>) : BaseRoute() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun configure(router: Router, vertx: Vertx) {
        router.post("/notification/*")
                .configureRestRoutingWithBody()
                .handler { ctx ->
                    val body = ctx.bodyAsJson

                    logger.info(body.encodePrettily())

                    val activity = body.mapTo(Activity::class.java)

                    if (activity.type == "conversationUpdate"
                            && activity.membersAdded != null && activity.membersAdded.isNotEmpty() && activity.membersAdded.first().id == appId) {
                        val conversationId = activity.conversation.id
                        val text = "Hi, this conversation id is: \"$conversationId\""
                        val message = Message(conversationId = conversationId, message = text)

                        messageSender.send(message)
                    }

                    ctx.response().end("<h1>Notified!</h1>")
                }
    }


}