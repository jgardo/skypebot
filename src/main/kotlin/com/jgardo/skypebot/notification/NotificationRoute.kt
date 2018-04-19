package com.jgardo.skypebot.notification

import com.google.inject.Guice
import com.jgardo.skypebot.server.BaseRoute
import com.jgardo.skypebot.message.model.Message
import com.jgardo.skypebot.notification.model.Activity
import com.jgardo.skypebot.util.TextTranslator
import io.jsonwebtoken.Jwts
import io.vertx.core.Vertx
import io.vertx.core.eventbus.MessageProducer
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router

class NotificationRoute(private val appId : String,
                        private val messageSender : MessageProducer<Message>,
                        private val textTranslator: TextTranslator) : BaseRoute() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun configure(router: Router, vertx: Vertx) {
        val injector = Guice.createInjector(NotificationModule(appId, messageSender, textTranslator))

        vertx.createHttpClient()

        router.post("/notification/*")
                .configureRestRoutingWithBody()
                .handler { ctx ->

                    val authorization = ctx.request().getHeader("Authorization")?: "not available"
                    logger.info("Authorization header: $authorization")

                    Jwts.parser()

                    val body = ctx.bodyAsJson

                    if (logger.isDebugEnabled) {
                        logger.debug(body.encodePrettily())
                    } else {
                        logger.info(body.encodePrettily())
                    }

                    val activity = body.mapTo(Activity::class.java)
                    val notificationController = injector.getInstance(NotificationController::class.java)

                    notificationController.notify(activity)

                    ctx.response().end("<h1>Notified!</h1>")
                }
    }


}