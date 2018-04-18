package com.jgardo.skypebot

import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.Injector
import com.jgardo.skypebot.authentication.AuthenticationVerticle
import com.jgardo.skypebot.message.MessageBusEvent
import com.jgardo.skypebot.message.MessageVerticle
import com.jgardo.skypebot.message.model.Message
import com.jgardo.skypebot.notification.NotificationController
import com.jgardo.skypebot.notification.NotificationModule
import com.jgardo.skypebot.notification.model.Activity
import com.jgardo.skypebot.util.VertxUtils
import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.core.logging.Logger
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

import io.vertx.core.logging.LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.SLF4JLogDelegateFactory
import io.vertx.core.logging.LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME



class SkypebotApplication : AbstractVerticle() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private lateinit var appId : String

    override fun start(fut: Future<Void>) {
        val config = ConfigRetriever.create(vertx)
        config.getConfig({ ar ->
            VertxUtils.wrap(ar, { json ->
                val port = json.getInteger("server.port")?: 8080
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

    private fun routes(vertx:Vertx) : Router {
        val route = Router.router(vertx)

        route.route("/*").handler(BodyHandler.create())

        val injector = Guice.createInjector(NotificationModule())

        route.route("/notification/*").handler { ctx ->
            val body = ctx.bodyAsJson

            logger.info(body.encodePrettily())

            val activity = body.mapTo(Activity::class.java)

            if (activity.type == "conversationUpdate"
                    && activity.membersAdded.isNotEmpty() && activity.membersAdded.first().id == appId) {
                val conversationId = activity.conversation.id
                val text = "Hi, this conversation id is: \"$conversationId\""
                val message = Message(conversationId = conversationId, message=text)

                vertx.eventBus().send(MessageBusEvent.SEND.eventName, Json.encode(message))
            }

            ctx.response().end("<h1>Notified!</h1>")
        }

        route.post("/message/*").handler { ctx ->
            val body = ctx.bodyAsJson

            vertx.eventBus().send(MessageBusEvent.SEND.eventName, Json.encode(body))

            ctx.response().end("<h1>Sent!</h1>")
        }

        return route
    }
}

fun main(args : Array<String>) {
    System.setProperty(LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory::class.java.name)

    val vertx = Vertx.vertx()!!

    val logger = LoggerFactory.getLogger(SkypebotApplication::class.java)
    logAllPropertiesIfDebug(logger, vertx)

    fun logStarted(name : String, ar:AsyncResult<String>) {
        VertxUtils.wrap(ar, {logger.info("Verticle $name started.")})
    }

    vertx.deployVerticle(MessageVerticle(), {ar -> logStarted("MessageVerticle", ar)})
    vertx.deployVerticle(SkypebotApplication(), {ar -> logStarted("SkypebotApplication", ar)})
    vertx.deployVerticle(AuthenticationVerticle(), {ar -> logStarted("AuthenticationVerticle", ar)})
}

private fun logAllPropertiesIfDebug(logger: Logger, vertx: Vertx) {
    if (logger.isDebugEnabled) {
        val retriever = ConfigRetriever.create(vertx)
        retriever.getConfig({ ar ->
            VertxUtils.wrap(ar, { json ->
                val res = json.map
                val shortened = res.mapValues { (key,value) ->
                    when {
                        Config.values()
                                .filter { e -> e.sensitive }
                                .map { e -> e.configName }
                                .contains(key) -> return@mapValues VertxUtils.shortenSensitiveString(value as String)
                        key.startsWith("receiver") -> return@mapValues VertxUtils.shortenSensitiveString(value as String)
                        else -> return@mapValues value
                    }
                }
                val sb = StringBuilder()
                shortened.map { (key, value) -> return@map "$key=$value" }
                        .forEach { str -> sb.appendln(str) }

                logger.debug("All configs:\n\n$sb")
            }, { e ->
                logger.error("Problem with getting config.",e)
            })
        })
    }
}
