package com.jgardo.skypebot

import com.jgardo.skypebot.authentication.AuthenticationVerticle
import com.jgardo.skypebot.config.Config
import com.jgardo.skypebot.message.MessageVerticle
import com.jgardo.skypebot.server.ServerVerticle
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

    val logger = LoggerFactory.getLogger(SkypebotApplication::class.java)
    logAllPropertiesIfDebug(logger, vertx)

    fun logStarted(name : String, ar:AsyncResult<String>) {
        VertxUtils.wrap(ar, {logger.info("Verticle $name started.")})
    }

    vertx.deployVerticle(MessageVerticle(), {ar -> logStarted("MessageVerticle", ar)})
    vertx.deployVerticle(ServerVerticle(), { ar -> logStarted("ServerVerticle", ar)})
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
