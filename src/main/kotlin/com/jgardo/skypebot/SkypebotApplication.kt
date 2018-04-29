package com.jgardo.skypebot

import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.PrivateModule
import com.google.inject.Module
import com.jgardo.skypebot.config.BasicConfig
import com.jgardo.skypebot.message.DirectMessageModule
import com.jgardo.skypebot.message.MessageVerticle
import com.jgardo.skypebot.notification.NotificationModule
import com.jgardo.skypebot.server.ServerModule
import com.jgardo.skypebot.server.ServerVerticle
import com.jgardo.skypebot.vertx.VertxConfigurationService
import com.jgardo.skypebot.vertx.VertxUtils
import io.vertx.config.ConfigRetriever
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.logging.LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME
import io.vertx.core.logging.SLF4JLogDelegateFactory

class SkypebotApplication(private vararg val modules:PrivateModule) {

    private val logger = LoggerFactory.getLogger(SkypebotApplication::class.java)

    fun run() {
        System.setProperty(LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory::class.java.name)
        val vertx = createVertx()

        val allModules: MutableCollection<Module> = prepareModules(vertx)
        val injector:Injector = Guice.createInjector(allModules.asIterable())

        val vertxConfigurationService = injector.getInstance(VertxConfigurationService::class.java)
        vertxConfigurationService.configure()

        deployVerticles(injector, vertx)
    }

    private fun createVertx() : Vertx {
        val vertx = Vertx.vertx()!!

        logAllPropertiesIfDebug(vertx)

        return vertx
    }
    private fun deployVerticles(injector: Injector, vertx: Vertx) {
        val serverVerticle: ServerVerticle = injector.getInstance(ServerVerticle::class.java)
        val messageVerticle: MessageVerticle = injector.getInstance(MessageVerticle::class.java)

        fun logStarted(name: String, ar: AsyncResult<String>) {
            VertxUtils.wrap(ar, { logger.info("Verticle $name started.") })
        }

        vertx.deployVerticle(messageVerticle, { ar -> logStarted("MessageVerticle", ar) })
        vertx.deployVerticle(serverVerticle, { ar -> logStarted("ServerVerticle", ar) })
    }

    private fun prepareModules(vertx: Vertx): MutableCollection<Module> {
        val modules = this.modules.toHashSet()
        val serverModule = ServerModule(modules, vertx)
        val allModules: MutableCollection<Module> = HashSet()
        for (module in modules) {
            allModules.add(module as Module)
        }
        allModules.add(serverModule as Module)
        return allModules
    }

    private fun logAllPropertiesIfDebug(vertx: Vertx) {
        if (logger.isDebugEnabled) {
            val retriever = ConfigRetriever.create(vertx)
            retriever.getConfig({ ar ->
                VertxUtils.wrap(ar, { json ->
                    val res = json.map
                    val shortened = res.mapValues { (key,value) ->
                        when {
                            BasicConfig.values()
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
}

fun main(args : Array<String>) {
    SkypebotApplication(
            NotificationModule(),
            DirectMessageModule()
    ).run()
}