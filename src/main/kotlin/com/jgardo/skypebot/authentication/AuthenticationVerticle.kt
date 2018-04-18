package com.jgardo.skypebot.authentication

import com.jgardo.skypebot.Config
import com.jgardo.skypebot.util.VertxUtils
import io.vertx.config.ConfigRetriever
import io.vertx.core.*
import io.vertx.core.eventbus.Message
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.codec.BodyCodec

class AuthenticationVerticle : AbstractVerticle() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private var client : WebClient? = null
    private val authenticationForm = MultiMap.caseInsensitiveMultiMap()
    override fun start(fut : Future<Void>) {
        val retriever = ConfigRetriever.create(vertx)
        retriever.getConfig({ ar ->
            VertxUtils.wrap(ar, {
            json ->
                val clientId = json.getString(Config.AUTHENTICATION_CLIENT_ID.configName)
                val clientSecret = json.getString(Config.AUTHENTICATION_CLIENT_SECRET.configName)
                authenticationForm.set("grant_type", "client_credentials")
                        .set("client_id", clientId) //MICROSOFT-APP-ID
                        .set("client_secret", clientSecret) //MICROSOFT-APP-PASSWORD
                        .set("scope", "https://api.botframework.com/.default")
                if (logger.isDebugEnabled) {
                    val sb = StringBuilder()
                            .appendln("Authentication properties:")
                            .appendln("clientId: ${VertxUtils.shortenSensitiveString(clientId)}")
                            .appendln("clientSecret: ${VertxUtils.shortenSensitiveString(clientSecret)}")
                    logger.debug(sb.toString())
                }
                fut.complete()
            }, { e ->
                logger.error("Startup error.", e)
                fut.fail(e)
            })
        })
        client = WebClient.create(vertx)
        vertx!!.eventBus().consumer("authenticate", this::authenticate)
        vertx!!.eventBus().send("authenticate", "")
    }

    private fun authenticate(message:Message<String>) {
        if (logger.isDebugEnabled) {
            logger.debug("Authenticating")
        }

        client!!.postAbs("https://login.microsoftonline.com/botframework.com/oauth2/v2.0/token").`as`(BodyCodec.jsonObject())
                .putHeader("Content-Type","application/x-www-form-urlencoded")
                .sendForm(authenticationForm, {ar ->

                    if (logger.isDebugEnabled) {
                        logger.debug("Authentication finishes!")
                    }

                    val result = ar.result()
                    val json = result.body()

                    if (result.statusCode() != 200) {
                        logger.error("Wrong status code in response. Full response: $result")
                        return@sendForm
                    }
                    val tokenType : String? = json.getString("token_type")!!
                    if (tokenType!! != "Bearer") {
                        logger.error("Wrong token_type in response. Full response: $result")
                        return@sendForm
                    }
                    val expiresIn = json.getLong("expires_in")!!
                    if (expiresIn < 0) {
                        logger.error("Invalid expires_in in response. Full response: $result")
                        return@sendForm
                    }

                    val accessToken = json.getString("access_token")!!
                    val map = vertx!!.sharedData().getLocalMap<String, String>("authentication")

                    if (logger.isDebugEnabled) {
                        logger.debug("Authenticated!")
                    }

                    map["accessToken"] = accessToken
                    message.reply("Success")

                    vertx.setTimer(expiresIn * 1000, {
                        vertx!!.eventBus().send("authenticate","")
                    })
                })
    }
}