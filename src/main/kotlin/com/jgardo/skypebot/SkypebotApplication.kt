package com.jgardo.skypebot

import com.google.inject.Guice
import com.google.inject.Injector
import com.jgardo.skypebot.config.Config
import com.jgardo.skypebot.message.DirectMessageModule
import com.jgardo.skypebot.message.MessageVerticle
import com.jgardo.skypebot.message.model.Message
import com.jgardo.skypebot.notification.NotificationModule
import com.jgardo.skypebot.server.ServerModule
import com.jgardo.skypebot.server.ServerVerticle
import com.jgardo.skypebot.util.ObjectMessageCodec
import com.jgardo.skypebot.util.VertxUtils
import io.vertx.config.ConfigRetriever
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.logging.LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME
import io.vertx.core.logging.SLF4JLogDelegateFactory

class SkypebotApplication

fun main(args : Array<String>) {
    System.setProperty(LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory::class.java.name)

    val vertx = Vertx.vertx()!!

    configureVertx(vertx)

    val logger = LoggerFactory.getLogger(SkypebotApplication::class.java)
    logAllPropertiesIfDebug(logger, vertx)

    val notificationModule = NotificationModule()
    val directMessageModule = DirectMessageModule()

    val serverModule = ServerModule(setOf(notificationModule, directMessageModule), vertx)
    val injector:Injector = Guice.createInjector(notificationModule, directMessageModule, serverModule)

    val serverVerticle : ServerVerticle = injector.getInstance(ServerVerticle::class.java)
    val messageVerticle : MessageVerticle = injector.getInstance(MessageVerticle::class.java)

    fun logStarted(name : String, ar:AsyncResult<String>) {
        VertxUtils.wrap(ar, {logger.info("Verticle $name started.")})
    }

    vertx.deployVerticle(messageVerticle, {ar -> logStarted("MessageVerticle", ar)})
    vertx.deployVerticle(serverVerticle, { ar -> logStarted("ServerVerticle", ar)})
}

private fun configureVertx(vertx: Vertx) {
    vertx.eventBus().registerDefaultCodec(Message::class.java, ObjectMessageCodec(Message::class.java))
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
