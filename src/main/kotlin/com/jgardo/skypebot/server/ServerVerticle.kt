package com.jgardo.skypebot.server

import com.jgardo.skypebot.config.Config
import com.jgardo.skypebot.message.MessageBusEvent
import com.jgardo.skypebot.message.MessageRoute
import com.jgardo.skypebot.message.model.Message
import com.jgardo.skypebot.notification.NotificationRoute
import com.jgardo.skypebot.util.TextTranslator
import com.jgardo.skypebot.util.ObjectMessageCodec
import com.jgardo.skypebot.util.VertxUtils
import io.vertx.config.ConfigRetriever
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router

class ServerVerticle(private val textTranslator: TextTranslator) : AbstractVerticle(){
    private val logger = LoggerFactory.getLogger(this::class.java)
    private lateinit var appId : String

    override fun start(fut: Future<Void>) {
        val config = ConfigRetriever.create(vertx)
        config.getConfig({ ar ->
            VertxUtils.wrap(ar, { json ->
                val port = json.getInteger(Config.SERVER_PORT.configName)?: 8080
                appId = json.getString(Config.APP_ID.configName)!!

                vertx
                        .createHttpServer()
                        .requestHandler(routes(vertx)::accept)
                        .listen(port) { res -> VertxUtils.wrap(res, { _ ->
                            logger.info("Http listener startup completed")
                            fut.complete()
                        },{ th ->
                            logger.error("Http listener startup failed", th)
                            fut.fail(th)
                        })}
            })
        })
    }

    private fun routes(vertx: Vertx) : Router {
        val router = Router.router(vertx)

        val messageSender = vertx.eventBus()
                .registerDefaultCodec(Message::class.java, ObjectMessageCodec(Message::class.java))
                .sender<Message>(MessageBusEvent.SEND.eventName)
                .exceptionHandler({ex->logger.error("Error when sending message", ex)})

        NotificationRoute(appId, messageSender, textTranslator).configure(router, vertx)
        MessageRoute(messageSender).configure(router,vertx)

        return router
    }

}