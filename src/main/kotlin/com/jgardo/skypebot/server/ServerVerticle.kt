package com.jgardo.skypebot.server

import com.jgardo.skypebot.config.BasicConfig
import com.jgardo.skypebot.util.VertxUtils
import io.vertx.config.ConfigRetriever
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import javax.inject.Inject

class ServerVerticle @Inject constructor(private val baseRoutes :java.util.Set<BaseRoute>) : AbstractVerticle(){
    private val logger = LoggerFactory.getLogger(this::class.java)
    private lateinit var appId : String

    override fun start(fut: Future<Void>) {
        val config = ConfigRetriever.create(vertx)
        config.getConfig({ ar ->
            VertxUtils.wrap(ar, { json ->
                val port = json.getInteger(BasicConfig.SERVER_PORT.configName)?: 8080
                appId = json.getString(BasicConfig.APP_ID.configName)!!

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

        baseRoutes.forEach {baseRoute -> (baseRoute as BaseRoute).configure(router, vertx)}

        return router
    }

}