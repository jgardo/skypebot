package com.jgardo.skypebot.notification

import com.google.inject.Guice
import com.jgardo.skypebot.server.BaseRoute
import com.jgardo.skypebot.message.model.Message
import com.jgardo.skypebot.notification.model.Activity
import com.jgardo.skypebot.util.TextTranslator
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.eventbus.MessageProducer
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.client.WebClient

class NotificationRoute(private val appId : String,
                        private val messageSender : MessageProducer<Message>,
                        private val textTranslator: TextTranslator) : BaseRoute() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun configure(router: Router, vertx: Vertx) {
        val injector = Guice.createInjector(NotificationModule(appId, messageSender, textTranslator))

        val webClient : WebClient = WebClient.create(vertx)
        val authorizator: NotificationAuthorizator = NotificationAuthorizator(webClient)

        router.post("/notification/*")
                .configureRestRoutingWithBody()
                .handler { ctx ->
                    val authorization = ctx.request().getHeader("Authorization")?: "not available"

                    authorizator.validate(authorization).compose { valid ->
                        if (valid) {
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
                            return@compose Future.succeededFuture<Void>()
                        } else {
                            ctx.response()
                                    .end("<h1>Not authorized!</h1>")
                            return@compose Future.succeededFuture<Void>()
                        }
                    }
                }

    }
}