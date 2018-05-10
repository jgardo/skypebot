package com.jgardo.skypebot.notification

import com.google.inject.Inject
import com.jgardo.skypebot.notification.model.Activity
import com.jgardo.skypebot.server.BaseRoute
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router

class NotificationRoute @Inject constructor(private val authorizator : NotificationAuthorizator,
                                            private val notificationController: NotificationController) : BaseRoute() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun configure(router: Router, vertx: Vertx) {
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

                            notificationController.notify(activity)

                            ctx.response().end("<h1>Notified!</h1>")
                            return@compose Future.succeededFuture<Void>()
                        } else {
                            ctx.response()
                                    .setStatusCode(401)
                                    .end("<h1>Unauthorized!</h1>")
                            return@compose Future.succeededFuture<Void>()
                        }
                    }
                }

    }
}