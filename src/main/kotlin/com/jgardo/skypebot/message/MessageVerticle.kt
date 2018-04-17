package com.jgardo.skypebot.message

import io.vertx.config.ConfigRetriever
import io.vertx.core.*
import io.vertx.core.eventbus.Message as VertxMessage
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.codec.BodyCodec
import com.jgardo.skypebot.message.model.Message
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import java.util.stream.Collectors

class MessageVerticle : AbstractVerticle() {
    private lateinit var client : WebClient

    private lateinit var receiversByName : Map<String, Any>
    private lateinit var appId : String
    private lateinit var baseUrl : String

    override fun start(fut : Future<Void>) {
        val retriever = ConfigRetriever.create(vertx)
        retriever.getConfig({ ar ->
            val json = ar.result()
            appId = json.getString("appid")!!
            baseUrl = json.getString("baseurl")!!

            receiversByName = json.fieldNames().stream()
                    .filter { name -> name.startsWith("receiver") }
                    .collect(Collectors.toMap({a -> a.substring("receiver.".length)}, {key -> json.getString(key)}))
            fut.complete()
        })
        client = WebClient.create(vertx)
        vertx!!.eventBus().consumer(MessageBusEvent.SEND.eventName, this::send)
    }

    private fun send(message:VertxMessage<String>) {
        val body = Json.decodeValue(message.body(), Message::class.java)
        val conversationId : String = if (body.receiver != null) receiversByName[body.receiver] as String
            else body.conversationId!!

        val accessToken = vertx.sharedData().getLocalMap<String,String>("authentication")["accessToken"]

        val json = JsonObject()
        json.put("text", body.message)
                .put("textFormat", "plain")
                .put("type", "message")
                .put("from", JsonObject().put("id",appId))
                .put("conversation", JsonObject().put("id",conversationId))
        client.postAbs("${baseUrl}v3/conversations/$conversationId/activities").`as`(BodyCodec.jsonObject())
                .putHeader("Authorization", "Bearer $accessToken")
                .putHeader("Content-Type","application/json")
                .sendJson(json, {ar ->
                    val result = ar.result()
                    val json = result.body()
                    println(json.getString("id"))
                })
    }
}