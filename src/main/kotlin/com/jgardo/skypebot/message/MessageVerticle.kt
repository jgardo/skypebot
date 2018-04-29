package com.jgardo.skypebot.message

import com.jgardo.skypebot.config.BasicConfig
import io.vertx.config.ConfigRetriever
import io.vertx.core.*
import io.vertx.core.eventbus.Message as VertxMessage
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.codec.BodyCodec
import com.jgardo.skypebot.message.model.Message
import com.jgardo.skypebot.message.exception.ReceiverNotFoundException
import com.jgardo.skypebot.util.VertxUtils
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import java.util.stream.Collectors
import javax.inject.Inject

class MessageVerticle @Inject constructor(private val messageAuthenticator: MessageAuthenticator) : AbstractVerticle() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private lateinit var client : WebClient
    private lateinit var receiversByName : Map<String, String>
    private lateinit var appId : String
    private lateinit var baseUrl : String

    override fun start(fut : Future<Void>) {
        val retriever = ConfigRetriever.create(vertx)
        initializeConfig(retriever, fut)
        client = WebClient.create(vertx)
        vertx!!.eventBus().consumer(MessageBusEvent.SEND.eventName, this::send)
    }

    private fun initializeConfig(retriever: ConfigRetriever, fut: Future<Void>) {
        retriever.getConfig({ ar ->
            VertxUtils.wrap(ar, { json ->
                appId = json.getString(BasicConfig.APP_ID.configName)?: VertxUtils.missingConfig(BasicConfig.APP_ID)
                baseUrl = json.getString(BasicConfig.BASE_URL.configName)?: VertxUtils.missingConfig(BasicConfig.BASE_URL)
                receiversByName = prepareReceivers(json)
                if (receiversByName.isEmpty()) {
                    logger.warn(IllegalStateException("There is no receiver registered. It's necessery to send message addressing by logical name."))
                }
                logOnStartup()
                fut.complete()
            }, { e ->
                fut.fail(e)
            })
        })
    }

    private fun prepareReceivers(json: JsonObject) : Map<String, String>{
        return json.fieldNames().stream()
                .filter { name -> name.startsWith("receiver") }
                .collect(Collectors.toMap({ a -> a.substring("receiver.".length) }, { key -> json.getString(key) }))
    }

    private fun logOnStartup() {
        if (logger.isDebugEnabled) {
            val sb = StringBuilder()
                    .appendln("Properties for sending messages:")
                    .appendln("appid: ${VertxUtils.shortenSensitiveString(appId)}")
                    .appendln("baseUrl: ${VertxUtils.shortenSensitiveString(baseUrl)}")

            logger.debug(sb.toString())

        }
        val shortenedRegisteredReceivers = receiversByName.mapValues { (_, v: String) -> VertxUtils.shortenSensitiveString(v) }
        logger.info("Registered receivers:$shortenedRegisteredReceivers")
    }

    private fun send(message:VertxMessage<Message>) {
        val body = message.body()
        val conversationId: String = getConversationId(body, body.receiver)
        val accessTokenFuture = messageAuthenticator.getAccessToken()

        accessTokenFuture.compose { accessToken ->
            val future : Future<String> = Future.future()
            logMessage(body, conversationId)
            val requestJson = prepareMessagePayload(body, conversationId)

            client.postAbs("${baseUrl}v3/conversations/$conversationId/activities").`as`(BodyCodec.jsonObject())
                    .putHeader("Authorization", "Bearer $accessToken")
                    .putHeader("Content-Type","application/json")
                    .sendJson(requestJson, {ar ->
                        if (ar.succeeded()) {
                            val json = ar.result().body()
                            val id = json.getString("id")
                            if (logger.isDebugEnabled) {
                                logger.debug("Message sent successfully. Message id: $id")
                            }
                            future.complete(id)
                        } else {
                            logger.error("Message sending failure.", ar.cause())
                            future.fail(ar.cause())
                        }
                    })
            return@compose future
        }
    }

    private fun prepareMessagePayload(body: Message, conversationId: String): JsonObject? {
        return JsonObject()
                .put("text", body.message)
                .put("textFormat", "plain")
                .put("type", "message")
                .put("from", JsonObject().put("id", appId))
                .put("conversation", JsonObject().put("id", conversationId))
    }

    private fun logMessage(body: Message, conversationId: String) {
        if (logger.isDebugEnabled) {
            val sb = StringBuilder()
                    .appendln("Sending message:")
                    .appendln("message: ${body.message}")
                    .appendln("conversation: $conversationId")
            logger.debug(sb.toString())
        }
    }

    private fun getConversationId(body: Message, receiver: String?): String {
        if (body.receiver != null && receiversByName[receiver] == null) {
            throw ReceiverNotFoundException(receiver!!)
        }

        return if (body.receiver != null) receiversByName[receiver] as String
        else body.conversationId!!
    }
}