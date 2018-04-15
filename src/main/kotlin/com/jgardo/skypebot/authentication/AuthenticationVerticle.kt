package com.jgardo.skypebot.authentication

import io.vertx.config.ConfigRetriever
import io.vertx.core.*
import io.vertx.core.eventbus.Message
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.codec.BodyCodec

class AuthenticationVerticle : AbstractVerticle() {
    private var client : WebClient? = null
    private val authenticationForm = MultiMap.caseInsensitiveMultiMap()
    override fun start(fut : Future<Void>) {
        val retriever = ConfigRetriever.create(vertx)
        retriever.getConfig({ ar ->
            val json = ar.result()
            val authentication = json.getJsonObject("authentication")
            val clientId = authentication.getString("clientId")
            val clientSecret = authentication.getString("clientSecret")
            authenticationForm.set("grant_type", "client_credentials")
                    .set("client_id", clientId) //MICROSOFT-APP-ID
                    .set("client_secret", clientSecret) //MICROSOFT-APP-PASSWORD
                    .set("scope", "https://api.botframework.com/.default")
            fut.complete()
        })
        client = WebClient.create(vertx)
        vertx!!.eventBus().consumer("authenticate", this::authenticate)
    }

    private fun authenticate(message:Message<String>) {
        client!!.postAbs("https://login.microsoftonline.com/botframework.com/oauth2/v2.0/token").`as`(BodyCodec.jsonObject())
                .putHeader("Content-Type","application/x-www-form-urlencoded")
                .sendForm(authenticationForm, {ar ->
                    val result = ar.result()
                    val json = result.body()

                    if (result.statusCode() != 200) {
                        println("Wrong status code in response. Full response: $result")
                        return@sendForm
                    }
                    val tokenType : String? = json.getString("token_type")!!
                    if (tokenType!! != "Bearer") {
                        println("Wrong token_type in response. Full response: $result")
                        return@sendForm
                    }
                    val expiresIn = json.getLong("expires_in")!!
                    if (expiresIn < 0) {
                        println("Invalid expires_in in response. Full response: $result")
                        return@sendForm
                    }

                    val accessToken = json.getString("access_token")!!
                    val map = vertx!!.sharedData().getLocalMap<String, String>("authentication")
                    map["accessToken"] = accessToken
                    message.reply("Success")

                    vertx.setTimer(expiresIn, {
                        vertx!!.eventBus().send("authenticate","")
                    })
                })
    }
}