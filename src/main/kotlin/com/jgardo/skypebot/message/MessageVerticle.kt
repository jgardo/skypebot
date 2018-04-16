package com.jgardo.skypebot.message

import io.vertx.config.ConfigRetriever
import io.vertx.core.*
import io.vertx.core.eventbus.Message as VertxMessage
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.codec.BodyCodec
import com.jgardo.skypebot.message.model.Message
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClientOptions

class MessageVerticle : AbstractVerticle() {
    private lateinit var client : WebClient

    private lateinit var receiversByName : Map<String, Any>
    private lateinit var accessToken : String

    override fun start(fut : Future<Void>) {
        val retriever = ConfigRetriever.create(vertx)
        retriever.getConfig({ ar ->
            val json = ar.result()
            val receivers = json.getJsonObject("receiver")
            accessToken = json.getJsonObject("authentication")
                    .getString("hardcodedAccessToken")
            receiversByName = receivers.map

            fut.complete()
        })
        client = WebClient.create(vertx)
        vertx!!.eventBus().consumer("send", this::send)
    }

    private fun send(message:VertxMessage<String>) {
        val body = Json.decodeValue(message.body(), Message::class.java)
        val conversationId : String = receiversByName[body.receiver] as String

        val accessToken = vertx.sharedData().getLocalMap<String,String>("authentication")["accessToken"]

        val json = JsonObject()
        json.put("text", body.message)
                .put("textFormat", "plain")
                .put("type", "message")
                .put("from", JsonObject().put("id","Heroku@uG-QH0SeEgs"))
                .put("conversation", JsonObject().put("id",conversationId))
        client.postAbs("https://webchat.botframework.com/v3/conversations/$conversationId/activities").`as`(BodyCodec.jsonObject())
                .putHeader("Authorization", "Bearer $accessToken")
                .putHeader("Content-Type","application/json")
                .sendJson(json, {ar ->
                    val result = ar.result()
                    val json = result.body()
                })
    }
}